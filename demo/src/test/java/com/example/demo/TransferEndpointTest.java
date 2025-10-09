package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.dto.TransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransferEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User sender;
    private User receiver;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        sender = userRepository.save(new User("Sender", "sender@example.com"));
        receiver = userRepository.save(new User("Receiver", "receiver@example.com"));

        Wallet senderWallet = new Wallet(sender);
        senderWallet.setBalance(1000.0); // give sender some balance
        walletRepository.save(senderWallet);

        walletRepository.save(new Wallet(receiver));
    }

    @Test
    public void testSuccessfulTransfer() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSenderId(sender.getId());
        request.setReceiverId(receiver.getId());
        request.setAmount(500.0);

        mockMvc.perform(post("/api/users/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful!"));

        // Check balances
        Wallet updatedSenderWallet = walletRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(sender.getId()))
                .findFirst().orElse(null);

        Wallet updatedReceiverWallet = walletRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(receiver.getId()))
                .findFirst().orElse(null);

        assert updatedSenderWallet != null;
        assert updatedReceiverWallet != null;

        assert updatedSenderWallet.getBalance() == 500.0;
        assert updatedReceiverWallet.getBalance() == 500.0;
    }

    @Test
    public void testInsufficientBalance() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSenderId(sender.getId());
        request.setReceiverId(receiver.getId());
        request.setAmount(1500.0); // more than sender balance

        mockMvc.perform(post("/api/users/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance!"));
    }
}
