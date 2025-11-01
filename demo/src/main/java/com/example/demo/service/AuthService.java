package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ✅ Signup using DTOs
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        if (request.getDateOfBirth() == null) {
            throw new RuntimeException("Date of Birth is required!");
        }

        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new RuntimeException("Younger than 18 — Account cannot be created.");
        }

        // Map Request DTO → Entity
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        // Create wallet for new user
        Wallet wallet = new Wallet(savedUser);
        walletRepository.save(wallet);

        // Map Entity → Response DTO
        return new SignupResponse(
                "User registered successfully!",
                savedUser.getName(),
                savedUser.getEmail(),
                age,
                wallet.getBalance()
        );
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                "Login successful!",
                token,
                user.getName(),
                user.getEmail(),
                wallet.getBalance()
        );
    }
}
