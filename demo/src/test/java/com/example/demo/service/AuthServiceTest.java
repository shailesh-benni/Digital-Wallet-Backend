package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_success() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("12345");
        user.setName("Test");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> response = authService.signup(user);

        assertEquals("Test", response.get("name"));
        assertEquals("test@gmail.com", response.get("email"));
        assertEquals(0.0, response.get("balance"));
        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void signup_emailExists_throwsException() {
        User user = new User();
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.signup(user));
        assertEquals("Email already exists!", ex.getMessage());
    }

    @Test
    void login_success() {
        String rawPassword = "12345";
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setName("Test");

        Wallet wallet = new Wallet(user);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("dummyToken");

        Map<String, Object> response = authService.login("test@gmail.com", rawPassword);

        assertEquals("Test", response.get("name"));
        assertEquals("test@gmail.com", response.get("email"));
        assertEquals("dummyToken", response.get("token"));
    }

    @Test
    void login_invalidCredentials_throwsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("test@gmail.com", "12345"));
        assertEquals("Invalid credentials", ex.getMessage());
    }
}
