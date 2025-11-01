package com.example.demo.dto;

public class SignupResponse {
    private String message;
    private String name;
    private String email;
    private int age;
    private double balance;

    public SignupResponse(String message, String name, String email, int age, double balance) {
        this.message = message;
        this.name = name;
        this.email = email;
        this.age = age;
        this.balance = balance;
    }

    // Getters
    public String getMessage() { return message; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public double getBalance() { return balance; }
}
