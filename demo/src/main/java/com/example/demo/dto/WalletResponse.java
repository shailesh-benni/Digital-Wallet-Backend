package com.example.demo.dto;

public class WalletResponse {

    private Long id;
    private String walletName;
    private Double balance;
    private Long userId;
    private String userName;
    private String userEmail;

    public WalletResponse(Long id, String walletName, Double balance, Long userId, String userName, String userEmail) {
        this.id = id;
        this.walletName = walletName;
        this.balance = balance;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    // ✅ Getters and Setters
    public Long getId() { return id; }
    public String getWalletName() { return walletName; }
    public Double getBalance() { return balance; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
}
