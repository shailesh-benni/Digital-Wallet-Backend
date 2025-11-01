package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getMyInfo(user));
    }

    @GetMapping("/me/balance")
    public ResponseEntity<?> getMyBalance(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getMyBalance(user));
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<?> getMyTransactions(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getMyTransactions(user));
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getAllUsers(user));
    }
}
