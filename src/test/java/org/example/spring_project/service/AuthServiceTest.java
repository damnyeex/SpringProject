package org.example.spring_project.service;

import org.example.spring_project.model.User;
import org.example.spring_project.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith JUnit 5 Mockito для обработки аннотаций
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Успешный логин с правильным паролем")
    void shouldLoginSuccessfully() {
        // Arrange
        String login = "admin";
        String rawPassword = "admin123";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User mockUser = new User(login, null, "ADMIN", null, "Иванов", "Иван", "Иванович");
        mockUser.setId(UUID.randomUUID().toString());

        when(userRepository.findByLogin(login)).thenReturn(mockUser);
        when(userRepository.getPasswordHash(login)).thenReturn(hashedPassword);
        when(jwtService.generateToken(any(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn("mocked_jwt_token");

        // Act
        String token = authService.login(login, rawPassword);

        // Assert
        assertEquals("mocked_jwt_token", token);
        verify(userRepository, times(1)).findByLogin(login);
    }

    @Test
    @DisplayName("Ошибка при неверном пароле")
    void shouldThrowExceptionOnInvalidPassword() {
        // Arrange
        String login = "admin";
        String wrongPassword = "wrong";
        String hashedPassword = BCrypt.hashpw("correct_password", BCrypt.gensalt());

        User mockUser = new User(login, null, "ADMIN", null, null, null, null);

        when(userRepository.findByLogin(login)).thenReturn(mockUser);
        when(userRepository.getPasswordHash(login)).thenReturn(hashedPassword);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(login, wrongPassword);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwtService, never()).generateToken(any(), anyString(), anyString(), any(), any(), any(), any());
    }
}
