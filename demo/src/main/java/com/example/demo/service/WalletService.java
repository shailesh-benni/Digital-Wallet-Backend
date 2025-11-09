package com.example.demo.service;

import com.example.demo.config.WalletConfig;
import com.example.demo.dto.InternalTransferRequest;
import com.example.demo.dto.InternalTransferResponse;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.WalletResponse;
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

import java.util.List;
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

    // ✅ Create wallet by name (for a specific user)
    public WalletResponse createWallet(User user, String walletName) {
        Wallet wallet = new Wallet();
        wallet.setWalletName(walletName);
        wallet.setBalance(0.0);
        wallet.setUser(user);

        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getWalletName(),
                savedWallet.getBalance(),
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> transferAmount(User sender, TransferRequest request) {

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                log.info("💸 [Attempt {}] Transfer: {} ({}) → {} | ₹{}",
                        attempt, sender.getEmail(), request.getFromWalletName(), request.getReceiverEmail(), request.getAmount());

                // 🔹 Refetch both users each retry to get latest version
                User freshSender = userRepository.findById(sender.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

                User receiver = userRepository.findByEmail(request.getReceiverEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

                // 🔹 Get sender's wallet by name
                Wallet senderWallet = walletRepository.findByUserAndWalletName(freshSender, request.getFromWalletName())
                        .orElseThrow(() -> new IllegalArgumentException("Sender wallet '" + request.getFromWalletName() + "' not found"));

                // 🔹 Get receiver's Primary wallet
                Wallet receiverWallet = walletRepository.findByUserAndWalletName(receiver, "Primary")
                        .orElseThrow(() -> new IllegalArgumentException("Receiver Primary wallet not found"));

                double amount = request.getAmount();

                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be greater than 0");
                }

                if (senderWallet.getBalance() < amount) {
                    throw new IllegalArgumentException("Insufficient balance in wallet '" + senderWallet.getWalletName() + "'");
                }

                // ✅ Perform transfer
                senderWallet.setBalance(senderWallet.getBalance() - amount);
                receiverWallet.setBalance(receiverWallet.getBalance() + amount);

                walletRepository.save(senderWallet);
                walletRepository.save(receiverWallet);

                log.info("✅ [Attempt {}] Transfer successful: ₹{} from '{}' → {}'s Primary wallet",
                        attempt, amount, senderWallet.getWalletName(), receiver.getEmail());

                return Map.of(
                        "message", "Transfer successful",
                        "fromWallet", senderWallet.getWalletName(),
                        "senderBalance", senderWallet.getBalance(),
                        "receiver", receiver.getEmail(),
                        "receiverWallet", receiverWallet.getWalletName(),
                        "receiverBalance", receiverWallet.getBalance()
                );

            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                log.warn("⚠️ [Attempt {}] OptimisticLockException → Retrying...", attempt);

                if (attempt >= maxRetries) {
                    log.error("❌ Max retries reached. Transaction failed for sender {}", sender.getEmail());
                    return Map.of(
                            "message", "Transaction failed due to concurrent updates. Please retry.",
                            "balance", getPrimaryWalletBalance(sender)
                    );
                }

                try {
                    long sleepTime = 200L * attempt;
                    log.info("⏳ Backing off for {} ms before retry...", sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}

            } catch (Exception e) {
                log.error("🔥 Unexpected error in transfer attempt {}: {}", attempt, e.getMessage(), e);
                return Map.of(
                        "message", "Unexpected transaction failure: " + e.getMessage(),
                        "balance", getPrimaryWalletBalance(sender)
                );
            }
        }

        return Map.of("message", "Transaction failed unexpectedly", "balance", getPrimaryWalletBalance(sender));
    }


    public Wallet loadMoneyToWallet(Wallet wallet, double amount) {
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepository.save(wallet);
    }


    // --- Actual money movement ---
    @Transactional(propagation = Propagation.MANDATORY)
    protected void performTransfer(User sender, User receiver, double amount) {
        // Use Primary wallet by default (you can later extend this to pick wallets by name)
        Wallet senderWallet = walletRepository.findTopByUserOrderByIdAsc(sender)
                .orElseThrow(() -> new IllegalStateException("Sender wallet not found"));
        Wallet receiverWallet = walletRepository.findTopByUserOrderByIdAsc(receiver)
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

    private double getPrimaryWalletBalance(User user) {
        return walletRepository.findTopByUserOrderByIdAsc(user)
                .map(Wallet::getBalance)
                .orElse(0.0);
    }

    @Transactional
    public Map<String, Object> loadMoney(User user, double amount) {
        int topupCount = transactionRepository.getTopUpCountToday(user.getId());
        double topupTotal = transactionRepository.getTotalTopUpToday(user.getId());

        if (topupCount >= walletConfig.getTopup().getMaxCountPerDay()) {
            return Map.of("message", "Daily top-up count exceeded", "balance", getPrimaryWalletBalance(user));
        }

        if (topupTotal + amount > walletConfig.getTopup().getMaxAmountPerDay()) {
            double remaining = walletConfig.getTopup().getMaxAmountPerDay() - topupTotal;
            return Map.of("message", "Daily top-up limit reached. You can top-up up to ₹" + remaining,
                    "balance", getPrimaryWalletBalance(user));
        }

        Wallet wallet = walletRepository.findTopByUserOrderByIdAsc(user)
                .orElseGet(() -> walletRepository.save(new Wallet(user, "Primary")));
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
        transactionRepository.save(new Transaction(user, amount, "SELF_CREDITED", null));

        log.info("💰 Wallet loaded for User ID: {} | Amount: {}", user.getId(), amount);
        return Map.of("message", "Wallet loaded successfully", "balance", wallet.getBalance());
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InternalTransferResponse internalTransfer(User user, InternalTransferRequest request)
    {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                log.info("🔁 [Attempt {}] Internal transfer (User {}): {} → {} | ₹{}",
                        attempt, user.getEmail(), request.getFromWalletName(), request.getToWalletName(), request.getAmount());

                // 🔹 Refetch user for latest version
                User freshUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // 🔹 Get both wallets
                Wallet fromWallet = walletRepository.findByUserAndWalletName(freshUser, request.getFromWalletName())
                        .orElseThrow(() -> new IllegalArgumentException("From wallet '" + request.getFromWalletName() + "' not found"));

                Wallet toWallet = walletRepository.findByUserAndWalletName(freshUser, request.getToWalletName())
                        .orElseThrow(() -> new IllegalArgumentException("To wallet '" + request.getToWalletName() + "' not found"));

                double amount = request.getAmount();

                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be greater than 0");
                }

                if (fromWallet.getBalance() < amount) {
                    throw new IllegalArgumentException("Insufficient balance in '" + fromWallet.getWalletName() + "' wallet");
                }

                // ✅ Perform internal transfer
                fromWallet.setBalance(fromWallet.getBalance() - amount);
                toWallet.setBalance(toWallet.getBalance() + amount);

                walletRepository.save(fromWallet);
                walletRepository.save(toWallet);

                log.info("✅ [Attempt {}] Internal transfer successful: ₹{} {} → {}",
                        attempt, amount, fromWallet.getWalletName(), toWallet.getWalletName());

                return new InternalTransferResponse(
                        "Internal transfer successful",
                        fromWallet.getWalletName(),
                        fromWallet.getBalance(),
                        toWallet.getWalletName(),
                        toWallet.getBalance()
                );


            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                log.warn("⚠️ [Attempt {}] OptimisticLockException → Retrying...", attempt);

                if (attempt >= maxRetries) {
                    log.error("❌ Max retries reached. Internal transfer failed for user {}", user.getEmail());
                    return new InternalTransferResponse("Unexpected transaction failure: " + e.getMessage(), null, 0, null, 0);

                }

                try {
                    long sleepTime = 200L * attempt;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}

            } catch (Exception e) {
                log.error("🔥 Error in internal transfer attempt {}: {}", attempt, e.getMessage(), e);
                return new InternalTransferResponse("Unexpected transaction failure: " + e.getMessage(), null, 0, null, 0);

            }
        }
        return new InternalTransferResponse("Unexpected transaction failure: " + "Transaction failed unexpectedly", null, 0, null, 0);

    }

}
