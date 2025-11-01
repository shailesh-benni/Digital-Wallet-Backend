package com.example.demo.mapper;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;

public class AuthMapper {

    private AuthMapper() {} // prevent instantiation

    public static User toEntity(SignupRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setDateOfBirth(request.getDateOfBirth());
        return user;
    }

    public static SignupResponse toSignupResponse(User user, Wallet wallet, int age) {
        return new SignupResponse(
                "User registered successfully!",
                user.getName(),
                user.getEmail(),
                age,
                wallet.getBalance()
        );
    }

    public static LoginResponse toLoginResponse(User user, Wallet wallet, String token) {
        return new LoginResponse(
                "Login successful!",
                token,
                user.getName(),
                user.getEmail(),
                wallet.getBalance()
        );
    }
}
