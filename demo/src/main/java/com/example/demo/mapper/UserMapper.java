package com.example.demo.mapper;

import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.dto.UserProfileResponseDTO;
import com.example.demo.dto.UserSummaryDTO;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {}

    // ✅ Convert SignupRequestDTO → User Entity
    public static User toEntity(SignupRequest dto) {
        return new User(dto.getName(), dto.getEmail(), dto.getPassword(), dto.getDateOfBirth());
    }

    // ✅ Convert Transaction → TransactionResponseDTO
    public static TransactionResponse toTransactionDTO(Transaction txn) {
        String relatedUserName = txn.getRelatedUser() != null
                ? txn.getRelatedUser().getName()
                : null;

        return new TransactionResponse(
                txn.getId(),
                txn.getAmount(),
                txn.getType(),
                txn.getCreatedAt(),
                relatedUserName
        );
    }

    // ✅ Convert List<Transaction> → List<TransactionResponseDTO>
    public static List<TransactionResponse> toTransactionDTOList(List<Transaction> txns) {
        return txns.stream()
                .map(UserMapper::toTransactionDTO)
                .collect(Collectors.toList());
    }

    // ✅ Convert User + Wallet + Transactions → UserProfileResponseDTO
    public static UserProfileResponseDTO toUserProfileDTO(User user, Wallet wallet, List<Transaction> transactions) {
        return new UserProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                wallet.getBalance(),
                toTransactionDTOList(transactions)
        );
    }

    // ✅ Convert User → UserSummaryDTO (used in /api/users/all)
    public static UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getName()
        );
    }
}
