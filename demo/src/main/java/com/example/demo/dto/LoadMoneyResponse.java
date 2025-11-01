package com.example.demo.dto;

public class LoadMoneyResponse {
    private String message;
    private double balance;

    public LoadMoneyResponse(String message, double balance) {
        this.message = message;
        this.balance = balance;
    }

    public String getMessage() { return message; }
    public double getBalance() { return balance; }
}
