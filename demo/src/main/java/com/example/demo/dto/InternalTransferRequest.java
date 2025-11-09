package com.example.demo.dto;


public class InternalTransferRequest {
    private String fromWalletName;
    private String toWalletName;

    public String getFromWalletName() {
        return fromWalletName;
    }

    public InternalTransferRequest(String fromWalletName, String toWalletName, double amount) {
        this.fromWalletName = fromWalletName;
        this.toWalletName = toWalletName;
        this.amount = amount;
    }

    public void setFromWalletName(String fromWalletName) {
        this.fromWalletName = fromWalletName;
    }

    public String getToWalletName() {
        return toWalletName;
    }

    public void setToWalletName(String toWalletName) {
        this.toWalletName = toWalletName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    private double amount;
}
