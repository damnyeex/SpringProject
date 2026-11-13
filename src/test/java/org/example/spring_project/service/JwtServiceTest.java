package org.example.spring_project.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String TEST_SECRET = "my_super_secret_key_for_tests_1234567890_abcdefghijklmnop";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
    }

    @Test
    @DisplayName("Должен генерировать и валидировать JWT токен")
    void shouldGenerateAndVerifyToken() {
        UUID userId = UUID.randomUUID();
        String login = "testUser";
        String role = "ADMIN";
        String lastname = "Иванов";
        String firstname = "Иван";
        String middlename = "Иванович";
        String tenantId = UUID.randomUUID().toString();

        String token = jwtService.generateToken(userId, login, role, lastname, firstname, middlename, tenantId);
        Claims claims = jwtService.verifyToken(token);

        assertNotNull(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(login, claims.get("login", String.class));
        assertEquals(role, claims.get("role", String.class));
        assertEquals(lastname, claims.get("lastname", String.class));
        assertEquals(tenantId, claims.get("tenantId", String.class));
    }
}