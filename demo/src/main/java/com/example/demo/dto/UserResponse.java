package com.example.demo.dto;

import java.util.List;
import com.example.demo.dto.TransactionResponse;

public class UserResponse {
    private String name;
    private String email;
    private double balance;
    private List<TransactionResponse> transactions;

    public UserResponse(String name, String email, double balance, List<TransactionResponse> transactions) {
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.transactions = transactions;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getBalance() { return balance; }
    public List<TransactionResponse> getTransactions() { return transactions; }
}
