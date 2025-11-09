package com.example.demo.dto;

public class TransferRequest {
    private String receiverEmail;
    private double amount;
    private String fromWalletName; // ✅ new field

    // Getters and Setters
    public String getReceiverEmail() {
        return receiverEmail;
    }
    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getFromWalletName() {
        return fromWalletName;
    }
    public void setFromWalletName(String fromWalletName) {
        this.fromWalletName = fromWalletName;
    }
}
