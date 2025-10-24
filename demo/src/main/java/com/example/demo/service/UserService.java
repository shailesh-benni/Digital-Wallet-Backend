package com.example.demo.service;

import com.example.demo.config.WalletConfig;
import com.example.demo.dto.TransferRequest;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WalletConfig walletConfig;

    // --- Token helper ---
    public User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.getEmailFromToken(token);
        return userRepository.findByEmail(email).orElse(null);
    }

    // --- User info ---
    public Map<String, Object> getMyInfo(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        List<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);

        return Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "balance", wallet.getBalance(),
                "transactions", transactions
        );
    }

    public Map<String, Object> getMyBalance(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        return Map.of("balance", wallet.getBalance());
    }

    public List<Transaction> getMyTransactions(User user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Map<String, Object>> getAllUsers(User currentUser) {
        List<User> users = userRepository.findAll();
        users.removeIf(u -> u.getId().equals(currentUser.getId()));

        return users.stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getName()
                ))
                .toList();
    }

    // --- Send money / transfer with daily limit ---
    public Map<String, Object> transferAmount(User sender, TransferRequest request) {
        User receiver = userRepository.findById(request.getReceiverId()).orElse(null);
        if (receiver == null) {
            return Map.of("message", "Receiver not found", "balance", getWalletBalance(sender));
        }

        double sentToday = transactionRepository.getTotalSentToday(sender.getId());
        double remainingLimit = walletConfig.getTransaction().getDailyLimit() - sentToday;

        if (remainingLimit <= 0) {
            return Map.of("message", "Daily transaction limit reached. No more transactions today!",
                    "balance", getWalletBalance(sender));
        }

        if (request.getAmount() > remainingLimit) {
            return Map.of("message", "You can send only ₹" + remainingLimit + " today",
                    "balance", getWalletBalance(sender));
        }

        Wallet senderWallet = walletRepository.findByUser(sender).orElse(new Wallet(sender));
        Wallet receiverWallet = walletRepository.findByUser(receiver).orElse(new Wallet(receiver));

        if (senderWallet.getBalance() < request.getAmount()) {
            return Map.of("message", "Insufficient balance", "balance", senderWallet.getBalance());
        }

        // Process transaction
        senderWallet.setBalance(senderWallet.getBalance() - request.getAmount());
        receiverWallet.setBalance(receiverWallet.getBalance() + request.getAmount());
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        transactionRepository.save(new Transaction(sender, -request.getAmount(), "DEBIT", receiver));
        transactionRepository.save(new Transaction(receiver, request.getAmount(), "CREDIT", sender));

        return Map.of(
                "message", "Transfer successful",
                "balance", senderWallet.getBalance()
        );
    }

    // Helper to get balance
    private double getWalletBalance(User user) {
        return walletRepository.findByUser(user).map(Wallet::getBalance).orElse(0.0);
    }

    // --- Load money / top-up with daily limit and max 3 times per day ---
    public Map<String, Object> loadMoney(User user, double amount) {
        int topupCount = transactionRepository.getTopUpCountToday(user.getId());
        double topupTotal = transactionRepository.getTotalTopUpToday(user.getId());

        if (topupCount >= walletConfig.getTopup().getMaxCountPerDay()) {
            return Map.of("message", "Daily top-up count exceeded",
                    "balance", getWalletBalance(user));
        }

        if (topupTotal + amount > walletConfig.getTopup().getMaxAmountPerDay()) {
            double remaining = walletConfig.getTopup().getMaxAmountPerDay() - topupTotal;
            return Map.of("message", "Daily top-up limit reached. You can top-up up to ₹" + remaining,
                    "balance", getWalletBalance(user));
        }

        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        transactionRepository.save(new Transaction(user, amount, "SELF_CREDITED", null));

        return Map.of(
                "message", "Wallet loaded successfully",
                "balance", wallet.getBalance()
        );
    }

}
