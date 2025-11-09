package com.example.demo.dto;

public class InternalTransferResponse {

    private String message;
    private String fromWallet;
    private double fromBalance;
    private String toWallet;
    private double toBalance;

    // 🔹 Constructors
    public InternalTransferResponse() {}

    public InternalTransferResponse(String message, String fromWallet, double fromBalance, String toWallet, double toBalance) {
        this.message = message;
        this.fromWallet = fromWallet;
        this.fromBalance = fromBalance;
        this.toWallet = toWallet;
        this.toBalance = toBalance;
    }

    // 🔹 Getters & Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFromWallet() { return fromWallet; }
    public void setFromWallet(String fromWallet) { this.fromWallet = fromWallet; }

    public double getFromBalance() { return fromBalance; }
    public void setFromBalance(double fromBalance) { this.fromBalance = fromBalance; }

    public String getToWallet() { return toWallet; }
    public void setToWallet(String toWallet) { this.toWallet = toWallet; }

    public double getToBalance() { return toBalance; }
    public void setToBalance(double toBalance) { this.toBalance = toBalance; }
}
