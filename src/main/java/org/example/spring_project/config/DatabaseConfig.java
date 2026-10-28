package org.example.spring_project.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcClient jdbcClient;

    public DatabaseInitializer(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void run(String... args) throws Exception {
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

        // Создание админа, если его нет
        var count = jdbcClient.sql("SELECT COUNT(*) FROM users WHERE login = 'admin'")
                .query(Integer.class)
                .single();

        if (count == 0) {
            String hash = org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt());
            jdbcClient.sql("INSERT INTO users (id, login, password, role) VALUES (gen_random_uuid(), 'admin', :pass, 'ADMIN')")
                    .param("pass", hash)
                    .update();
            System.out.println("Default admin created!");
        } else {
            System.out.println("Admin already exists.");
        }

        System.out.println("=== Database Initialization Complete ===");
    }
}
