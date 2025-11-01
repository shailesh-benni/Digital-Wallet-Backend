package com.example.demo.dto;

public class LoginResponse {
    private String message;
    private String token;
    private String name;
    private String email;
    private double balance;

    public LoginResponse(String message, String token, String name, String email, double balance) {
        this.message = message;
        this.token = token;
        this.name = name;
        this.email = email;
        this.balance = balance;
    }

    // Getters
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getBalance() { return balance; }
}
