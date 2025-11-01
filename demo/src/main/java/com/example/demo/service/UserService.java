package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // --- ✅ Extract user from token ---
    public User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;

        String email = jwtUtil.getEmailFromToken(token);
        return userRepository.findByEmail(email).orElse(null);
    }

    // --- ✅ My Info (user + wallet + transactions) ---
    public UserProfileResponseDTO getMyInfo(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        List<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);

        List<TransactionResponse> txList = transactions.stream()
                .map(TransactionMapper::toDTO)
                .toList();

        return new UserProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                wallet.getBalance(),
                txList
        );
    }

    // --- ✅ My Balance ---
    public WalletBalanceResponseDTO getMyBalance(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        return new WalletBalanceResponseDTO(wallet.getBalance());
    }

    // --- ✅ My Transactions ---
    public List<TransactionResponse> getMyTransactions(User user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    // --- ✅ Get all users except current ---
    public List<UserSummaryDTO> getAllUsers(User currentUser) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .map(UserMapper::toSummaryDTO)
                .toList();
    }
}
