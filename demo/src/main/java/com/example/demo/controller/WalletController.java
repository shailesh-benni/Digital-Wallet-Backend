package com.example.demo.controller;

import com.example.demo.dto.TransferRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                            @RequestBody TransferRequest request) {
        User sender = userService.getUserFromToken(authHeader);
        if (sender == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(walletService.transferAmount(sender, request));
    }

    @PostMapping("/load")
    public ResponseEntity<?> loadMoney(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                       @RequestBody Map<String, Double> request) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        double amount = request.getOrDefault("amount", 0.0);
        return ResponseEntity.ok(walletService.loadMoney(user, amount));
    }
}
