package com.example.demo.controller;

import com.example.demo.dto.CreateWalletRequest;
import com.example.demo.dto.InternalTransferRequest;
import com.example.demo.dto.InternalTransferResponse;
import com.example.demo.dto.TransferRequest;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    // ✅ Transfer amount between wallets/users
    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                            @RequestBody TransferRequest request) {
        User sender = userService.getUserFromToken(authHeader);
        if (sender == null)
            return ResponseEntity.status(401).body("Unauthorized");

        return ResponseEntity.ok(walletService.transferAmount(sender, request));
    }

    // ✅ Load money into a specific wallet
    @PostMapping("/load")
    public ResponseEntity<?> loadMoney(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                       @RequestBody Map<String, Object> request) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");

        String walletName = (String) request.get("walletName");
        double amount = Double.parseDouble(request.getOrDefault("amount", 0.0).toString());

        Wallet wallet = walletRepository.findByUserAndWalletName(user, walletName).orElse(null);
        if (wallet == null)
            return ResponseEntity.badRequest().body("Wallet '" + walletName + "' not found for this user");

        return ResponseEntity.ok(walletService.loadMoneyToWallet(wallet, amount));
    }

    // ✅ Create new wallet for logged-in user
    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                          @RequestBody CreateWalletRequest request) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");

        return ResponseEntity.ok(walletService.createWallet(user, request.getWalletName()));
    }

    // ✅ Get all wallets for the logged-in user
    @GetMapping("/my-wallets")
    public ResponseEntity<?> getMyWallets(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        if (user == null)
            return ResponseEntity.status(401).body("Unauthorized");

        List<Wallet> wallets = walletRepository.findByUser(user);
        return ResponseEntity.ok(wallets);
    }

    // ✅ (Keep) Fetch all wallets for a user by ID (admin view)
    @GetMapping("/user/{userId}")
    public List<Wallet> getUserWallets(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return walletRepository.findByUser(user);
    }

    @PostMapping("/transfer/internal")
    public ResponseEntity<InternalTransferResponse> internalTransfer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody InternalTransferRequest request) {

        User user = userService.getUserFromToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(new InternalTransferResponse("Unauthorized", null, 0, null, 0));
        }

        return ResponseEntity.ok(walletService.internalTransfer(user, request));
    }


}
