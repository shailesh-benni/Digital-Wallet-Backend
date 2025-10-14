
package com.example.demo;

import com.example.demo.util.JwtUtil;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;
    private User user;

    @BeforeEach
    void setup() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("David", "david@example.com", new BCryptPasswordEncoder().encode("pass123"));
        userRepository.save(user);
        walletRepository.save(new Wallet(user));

        token = jwtUtil.generateToken(user.getEmail());
    }

    @Test
    void loadMoney_shouldIncreaseBalance() throws Exception {
        String json = """
            { "amount": 1000.0 }
        """;

        mockMvc.perform(post("/api/users/me/load")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.message").value("Wallet loaded successfully"));
    }

    @Test
    void transferMoney_shouldDecreaseSenderAndIncreaseReceiver() throws Exception {
        User receiver = new User("Eve", "eve@example.com", new BCryptPasswordEncoder().encode("pass123"));
        userRepository.save(receiver);
        walletRepository.save(new Wallet(receiver));

        // Load some balance for sender
        Wallet senderWallet = walletRepository.findByUser(user).get();
        senderWallet.setBalance(2000.0);
        walletRepository.save(senderWallet);

        String json = """
            {
                "receiverId": %d,
                "amount": 500.0
            }
        """.formatted(receiver.getId());

        mockMvc.perform(post("/api/users/transfer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"))
                .andExpect(jsonPath("$.balance").value(1500.0));
    }

    @Test
    void transferMoney_insufficientBalance_shouldFail() throws Exception {
        User receiver = new User("Eve", "eve@example.com", new BCryptPasswordEncoder().encode("pass123"));
        userRepository.save(receiver);
        walletRepository.save(new Wallet(receiver));

        String json = """
            {
                "receiverId": %d,
                "amount": 500.0
            }
        """.formatted(receiver.getId());

        mockMvc.perform(post("/api/users/transfer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance"));
    }
}
