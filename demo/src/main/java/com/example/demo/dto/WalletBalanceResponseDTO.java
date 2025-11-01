package com.example.demo.dto;

public class WalletBalanceResponseDTO {
    private Double balance;

    public WalletBalanceResponseDTO(Double balance) {
        this.balance = balance;
    }

    public Double getBalance() { return balance; }
}
