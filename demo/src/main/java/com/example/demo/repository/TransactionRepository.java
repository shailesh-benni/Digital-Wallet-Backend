package com.example.demo.repository;

import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Return transactions sorted by createdAt descending (most recent first)
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}