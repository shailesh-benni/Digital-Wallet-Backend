package com.example.demo.dto;

import com.example.demo.dto.TransactionResponse;
import java.util.List;

public class UserProfileResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private Double balance;
    private List<TransactionResponse> transactions;

    public UserProfileResponseDTO(Long id, String name, String email, Integer age, Double balance, List<TransactionResponse> transactions) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.balance = balance;
        this.transactions = transactions;
    }

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }
    public Double getBalance() { return balance; }
    public List<TransactionResponse> getTransactions() { return transactions; }
}
