package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    // ✅ Transaction type: SELF_CREDITED (top-up), DEBIT (sent), CREDIT (received)
    private String type;

    // ✅ Automatically set creation time
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIncludeProperties({"id", "name", "email"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "related_user_id")
    @JsonIncludeProperties({"id", "name", "email"})
    private User relatedUser;

    public Transaction() {}

    public Transaction(User user, Double amount, String type, User relatedUser) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.relatedUser = relatedUser;
        this.createdAt = LocalDateTime.now(); // ✅ Automatically set current timestamp
    }

    // getters and setters
    public Long getId() { return id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getRelatedUser() { return relatedUser; }
    public void setRelatedUser(User relatedUser) { this.relatedUser = relatedUser; }
}
