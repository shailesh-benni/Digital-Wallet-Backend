package com.example.demo.dto;

public class TransferRequest {
    private Long receiverId;
    private double amount;

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
