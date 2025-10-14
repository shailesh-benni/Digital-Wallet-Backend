package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> signup(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet(savedUser);
        walletRepository.save(wallet);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("balance", wallet.getBalance());

        return response;
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet(user));
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("token", token);
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("balance", wallet.getBalance());

        return response;
    }
}
