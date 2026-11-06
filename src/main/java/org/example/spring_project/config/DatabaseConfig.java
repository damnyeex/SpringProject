package org.example.spring_project.config;

import org.example.spring_project.security.PasswordFilter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatabaseConfig implements CommandLineRunner {

    private final JdbcClient jdbcClient;

    public DatabaseConfig(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Initializing Database ===");

        // Создание таблиц
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS tenants (
                id UUID PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                description TEXT
            )
        """).update();

        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                login VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(10) NOT NULL,
                firstname VARCHAR(50),
                lastname VARCHAR(50),
                middlename VARCHAR(50),
                tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL
            )
        """).update();


        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM users WHERE login = 'admin'")
                .query(Integer.class)
                .single();

        if (count == 0) {

            String hash = PasswordFilter.hash("admin123");
            UUID adminId = UUID.randomUUID();

            jdbcClient.sql("INSERT INTO users (id, login, password, role) VALUES (:id, 'admin', :pass, 'ADMIN')")
                    .param("id", adminId)
                    .param("pass", hash)
                    .update();

            System.out.println("Default admin created with ID: " + adminId);
        } else {
            System.out.println("Admin already exists.");
        }

        System.out.println("=== Database Initialization Complete ===");
    }
}
