package com.example.demo.mapper;

import com.example.demo.dto.TransactionResponse;
import com.example.demo.model.Transaction;

public class TransactionMapper {
    public static TransactionResponse toDTO(Transaction tx) {
        String relatedName = tx.getRelatedUser() != null ? tx.getRelatedUser().getName() : null;
        return new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getType(),
                tx.getCreatedAt(),
                relatedName
        );
    }
}
