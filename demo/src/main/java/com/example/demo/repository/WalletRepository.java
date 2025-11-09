package com.example.demo.repository;

import com.example.demo.model.Wallet;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // 🔹 Returns first wallet created for the user (used during login)
    Optional<Wallet> findTopByUserOrderByIdAsc(User user);

    // 🔹 Returns all wallets for a user
    List<Wallet> findByUser(User user);

    // 🔹 Returns a specific wallet by name
    Optional<Wallet> findByUserAndWalletName(User user, String walletName);

    // ✅ Add this one — checks existence directly
    boolean existsByUserAndWalletName(User user, String walletName);
}
