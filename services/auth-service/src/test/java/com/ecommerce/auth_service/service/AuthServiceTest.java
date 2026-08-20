package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.LoginRequest;
import com.ecommerce.auth_service.dto.RegisterRequest;
import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.EmailAlreadyInUseException;
import com.ecommerce.auth_service.exception.InvalidCredentialsException;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(jwtService.generateAccessToken(request.email(), "USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(request.email())).thenReturn("refresh-token");

        AuthResult result = authService.register(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.body().email()).isEqualTo(request.email());
        assertThat(result.body().role()).isEqualTo("USER");
        verify(userRepository).save(argThat(u -> u.getEmail().equals(request.email()) && u.getPasswordHash().equals("hashed")));
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyInUseException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = User.builder()
                .email(request.email())
                .passwordHash("hashed")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResult result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
    }

    @Test
    void login_wrongPassword_throws() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        User user = User.builder()
                .email(request.email())
                .passwordHash("hashed")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throws() {
        LoginRequest request = new LoginRequest("missing@example.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
