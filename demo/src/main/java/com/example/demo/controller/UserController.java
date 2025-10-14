package com.example.demo.controller;

import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.dto.TransferRequest;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getMyInfo(user));
    }

    @GetMapping("/me/balance")
    public ResponseEntity<?> getMyBalance(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getMyBalance(user));
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<?> getMyTransactions(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getMyTransactions(user));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getAllUsers(user));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                            @RequestBody TransferRequest request) {
        User sender = userService.getUserFromToken(authHeader);
        if (sender == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.transferAmount(sender, request));
    }

    @PostMapping("/me/load")
    public ResponseEntity<?> loadMoney(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                       @RequestBody Map<String, Double> request) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");

        double amount = request.getOrDefault("amount", 0.0);
        return ResponseEntity.ok(userService.loadMoney(user, amount));
    }
}
