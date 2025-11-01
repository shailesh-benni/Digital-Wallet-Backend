package com.example.demo.dto;

import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private double amount;
    private String type;
    private LocalDateTime createdAt;
    private String relatedUserName;

    public TransactionResponse(Long id, double amount, String type, LocalDateTime createdAt, String relatedUserName) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
        this.relatedUserName = relatedUserName;
    }

    // Getters
    public Long getId() { return id; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRelatedUserName() { return relatedUserName; }
}
