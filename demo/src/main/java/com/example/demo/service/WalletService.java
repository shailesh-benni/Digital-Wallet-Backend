package com.example.demo.service;

import com.example.demo.config.WalletConfig;
import com.example.demo.dto.TransferRequest;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletConfig walletConfig;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository,
                         TransactionRepository transactionRepository, WalletConfig walletConfig) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.walletConfig = walletConfig;
    }

    // ------------------ TRANSFER AMOUNT ------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> transferAmount(User sender, TransferRequest request) {

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                log.info("💸 [Attempt {}] Transfer: {} → {} | ₹{}", attempt, sender.getId(), request.getReceiverId(), request.getAmount());

                // Refetch both users inside the retry loop to get the latest version each time
                User freshSender = userRepository.findById(sender.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
                User receiver = userRepository.findById(request.getReceiverId())
                        .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

                performTransfer(freshSender, receiver, request.getAmount());

                log.info("✅ [Attempt {}] Transfer completed successfully.", attempt);
                return Map.of("message", "Transfer successful", "balance", getWalletBalance(freshSender));

            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                log.warn("⚠️ [Attempt {}] OptimisticLockException → Retrying...", attempt);

                if (attempt >= maxRetries) {
                    log.error("❌ Max retries reached. Transaction failed for sender {}", sender.getId());
                    return Map.of("message", "Transaction failed due to concurrent updates. Please retry.",
                            "balance", getWalletBalance(sender));
                }

                try {
                    // 💤 Sleep before retry — exponential backoff
                    long sleepTime = 200L * attempt; // 200ms, 400ms, 600ms
                    log.info("⏳ Backing off for {} ms before retry...", sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                log.error("🔥 Unexpected error in transfer attempt {}: {}", attempt, e.getMessage(), e);
                return Map.of("message", "Unexpected transaction failure", "balance", getWalletBalance(sender));
            }
        }

        return Map.of("message", "Transaction failed unexpectedly", "balance", getWalletBalance(sender));
    }

    // --- Actual money movement ---
    @Transactional(propagation = Propagation.MANDATORY)
    protected void performTransfer(User sender, User receiver, double amount) {
        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new IllegalStateException("Sender wallet not found"));
        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new IllegalStateException("Receiver wallet not found"));

        if (senderWallet.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance for user " + sender.getId());
        }

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        transactionRepository.save(new Transaction(sender, -amount, "DEBIT", receiver));
        transactionRepository.save(new Transaction(receiver, amount, "CREDIT", sender));
    }

    private double getWalletBalance(User user) {
        return walletRepository.findByUser(user)
                .map(Wallet::getBalance)
                .orElse(0.0);
    }

    @Transactional
    public Map<String, Object> loadMoney(User user, double amount) {
        int topupCount = transactionRepository.getTopUpCountToday(user.getId());
        double topupTotal = transactionRepository.getTotalTopUpToday(user.getId());

        if (topupCount >= walletConfig.getTopup().getMaxCountPerDay()) {
            return Map.of("message", "Daily top-up count exceeded", "balance", getWalletBalance(user));
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

        log.info("💰 Wallet loaded for User ID: {} | Amount: {}", user.getId(), amount);
        return Map.of("message", "Wallet loaded successfully", "balance", wallet.getBalance());
    }
}
