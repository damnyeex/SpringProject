package org.example.spring_project.repository;

import org.example.spring_project.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// @JdbcTest настраивает in-memory H2 БД и JdbcClient
@JdbcTest
// Передача репозитория в тестовый контекст
@Import(UserRepository.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUp() {
        // 1. Создание таблицы в пустой in-memory БД (имитация DatabaseConfig)
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                login VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(10) NOT NULL,
                firstname VARCHAR(50),
                lastname VARCHAR(50),
                middlename VARCHAR(50),
                tenant_id UUID
            )
        """).update();

        // 2. Очистка таблицы (на случай, если тестов несколько и они используют одну БД)
        jdbcClient.sql("DELETE FROM users").update();

        // 3. Вставка тестовых данных
        jdbcClient.sql("INSERT INTO users (id, login, password, role) VALUES (:id, 'testuser', 'hash', 'USER')")
                .param("id", UUID.randomUUID())
                .update();
    }

    @Test
    @DisplayName("Поиск пользователя по логину")
    void shouldFindUserByLogin() {
        // Act
        User user = userRepository.findByLogin("testuser");

        // Assert
        assertNotNull(user);
        assertEquals("testuser", user.getLogin());
        assertEquals("USER", user.getRole());
    }

    @Test
    @DisplayName("Создание пользователя")
    void shouldCreateUser() {
        // Arrange
        User newUser = new User("newuser", "pass", "ADMIN", null, "Петров", "Петр", "Петрович");
        newUser.setId(UUID.randomUUID().toString());

        // Act
        userRepository.create(newUser, "raw_pass");

        // Assert
        User savedUser = userRepository.findByLogin("newuser");
        assertNotNull(savedUser);
        assertEquals(newUser.getId(), savedUser.getId());
    }
}