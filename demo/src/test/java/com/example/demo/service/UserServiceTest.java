package com.example.demo.service;

import com.example.demo.dto.TransferRequest;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void transferAmount_success() {
        User sender = new User(); sender.setId(1L); sender.setEmail("a@test.com");
        User receiver = new User(); receiver.setId(2L); receiver.setEmail("b@test.com");

        Wallet senderWallet = new Wallet(sender); senderWallet.setBalance(100.0);
        Wallet receiverWallet = new Wallet(receiver); receiverWallet.setBalance(50.0);

        TransferRequest req = new TransferRequest();
        req.setReceiverId(2L);
        req.setAmount(30.0);

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUser(sender)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUser(receiver)).thenReturn(Optional.of(receiverWallet));

        Map<String, Object> response = userService.transferAmount(sender, req);

        assertEquals(70.0, senderWallet.getBalance());
        assertEquals("Transfer successful", response.get("message"));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transferAmount_insufficientBalance_throwsException() {
        User sender = new User(); sender.setId(1L);
        User receiver = new User(); receiver.setId(2L);

        Wallet senderWallet = new Wallet(sender); senderWallet.setBalance(10.0);
        Wallet receiverWallet = new Wallet(receiver); receiverWallet.setBalance(0.0);

        TransferRequest req = new TransferRequest(); req.setReceiverId(2L); req.setAmount(50.0);

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUser(sender)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUser(receiver)).thenReturn(Optional.of(receiverWallet));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.transferAmount(sender, req));
        assertEquals("Insufficient balance", ex.getMessage());
    }

    @Test
    void loadMoney_success() {
        User user = new User();
        Wallet wallet = new Wallet(user); wallet.setBalance(0.0);

        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        Map<String, Object> response = userService.loadMoney(user, 100);

        assertEquals(100.0, wallet.getBalance());
        assertEquals("Wallet loaded successfully", response.get("message"));
        verify(transactionRepository).save(any(Transaction.class));
    }
}
