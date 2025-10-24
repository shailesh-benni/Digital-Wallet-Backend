package com.example.demo.repository;

import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Return transactions sorted by createdAt descending (most recent first)
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);

    // --- Top-up queries ---

    // 1️⃣ Number of top-ups today
    @Query("SELECT COUNT(t) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'SELF_CREDITED' " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    Integer getTopUpCountToday(@Param("userId") Long userId);

    // 2️⃣ Total top-up amount today
    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'SELF_CREDITED' " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    Double getTotalTopUpToday(@Param("userId") Long userId);

    // --- Spending queries ---

    // 3️⃣ Total amount sent today (DEBIT transactions)
    @Query("SELECT COALESCE(SUM(-t.amount),0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'DEBIT' " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    Double getTotalSentToday(@Param("userId") Long userId);
}
