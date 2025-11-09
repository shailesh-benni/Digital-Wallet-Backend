package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String walletName;

    private Double balance = 0.0;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public Wallet() {}

    public Wallet(User user, String walletName) {
        this.user = user;
        this.walletName = walletName;
        this.balance = 0.0;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
