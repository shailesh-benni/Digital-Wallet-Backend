package com.example.demo.dto;

public class TransferResponse {
    private String message;
    private double balance;

    public TransferResponse(String message, double balance) {
        this.message = message;
        this.balance = balance;
    }

    public String getMessage() { return message; }
    public double getBalance() { return balance; }
}
