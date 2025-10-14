package com.example.demo.controller;

import com.example.demo.dto.TransferRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User("Test User", "test@example.com", "12345");
        user.setId(1L);
        token = "Bearer dummyToken";
    }

    @Test
    void getMyInfo_success() throws Exception {
        Map<String, Object> response = Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "balance", 100.0,
                "transactions", List.of()
        );
        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.getMyInfo(user)).thenReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getName()));
    }

    @Test
    void getMyInfo_invalid_token() throws Exception {
        when(userService.getUserFromToken("invalid")).thenReturn(null);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyBalance_success() throws Exception {
        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.getMyBalance(user)).thenReturn(Map.of("balance", 50.0));

        mockMvc.perform(get("/api/users/me/balance")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.0));
    }

    @Test
    void getMyBalance_invalid_token() throws Exception {
        when(userService.getUserFromToken("invalid")).thenReturn(null);

        mockMvc.perform(get("/api/users/me/balance")
                        .header(HttpHeaders.AUTHORIZATION, "invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transferAmount_success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setReceiverId(2L);
        request.setAmount(20.0);

        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.transferAmount(user, request)).thenReturn(Map.of(
                "message", "Transfer successful",
                "balance", 80.0
        ));

        mockMvc.perform(post("/api/users/transfer")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"));
    }

    @Test
    void transferAmount_insufficient_balance() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setReceiverId(2L);
        request.setAmount(200.0);

        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.transferAmount(user, request)).thenThrow(new RuntimeException("Insufficient balance"));

        mockMvc.perform(post("/api/users/transfer")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferAmount_invalid_receiver() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setReceiverId(999L);
        request.setAmount(20.0);

        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.transferAmount(user, request)).thenThrow(new RuntimeException("Receiver not found"));

        mockMvc.perform(post("/api/users/transfer")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferAmount_invalid_token() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setReceiverId(2L);
        request.setAmount(20.0);

        when(userService.getUserFromToken("invalid")).thenReturn(null);

        mockMvc.perform(post("/api/users/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loadMoney_success() throws Exception {
        Map<String, Double> request = Map.of("amount", 100.0);

        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.loadMoney(user, 100.0)).thenReturn(Map.of(
                "message", "Wallet loaded successfully",
                "balance", 200.0
        ));

        mockMvc.perform(post("/api/users/me/load")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wallet loaded successfully"));
    }

    @Test
    void loadMoney_invalid_amount() throws Exception {
        Map<String, Double> request = Map.of("amount", -10.0);

        when(userService.getUserFromToken(token)).thenReturn(user);
        when(userService.loadMoney(user, -10.0)).thenThrow(new RuntimeException("Amount must be > 0"));

        mockMvc.perform(post("/api/users/me/load")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loadMoney_invalid_token() throws Exception {
        Map<String, Double> request = Map.of("amount", 100.0);

        when(userService.getUserFromToken("invalid")).thenReturn(null);

        mockMvc.perform(post("/api/users/me/load")
                        .header(HttpHeaders.AUTHORIZATION, "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
