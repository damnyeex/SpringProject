package org.example.spring_project.repository;

import org.example.spring_project.model.User;
import org.example.spring_project.security.PasswordFilter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    // Внедрение зависимости через конструктор (SOLID)
    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Аналог твоего mapUser
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User u = new User(
                rs.getString("login"),
                null, // пароль не возвращаем
                rs.getString("role"),
                rs.getString("tenant_id"),
                rs.getString("lastname"),
                rs.getString("firstname"),
                rs.getString("middlename")
        );
        u.setId(rs.getObject("id").toString());
        return u;
    };

    public User findByLogin(String login) {
        return jdbcClient.sql("SELECT id, login, role, firstname, lastname, middlename, tenant_id FROM users WHERE login = :login")
                .param("login", login)
                .query(USER_ROW_MAPPER)
                .optional()
                .orElse(null);
    }

    public User findById(UUID id) {
        return jdbcClient.sql("SELECT id, login, role, firstname, lastname, middlename, tenant_id FROM users WHERE id = :id")
                .param("id", id)
                .query(USER_ROW_MAPPER)
                .optional()
                .orElse(null);
    }

    public List<User> findAllByTenantId(String tenantId) {
        return jdbcClient.sql("SELECT id, login, role, firstname, lastname, middlename, tenant_id FROM users WHERE tenant_id = :tenantId")
                .param("tenantId", UUID.fromString(tenantId))
                .query(USER_ROW_MAPPER)
                .list();
    }

    public List<User> findAll() {
        return jdbcClient.sql("SELECT id, login, role, firstname, lastname, middlename, tenant_id FROM users")
                .query(USER_ROW_MAPPER)
                .list();
    }

    public void update(User user) {
        jdbcClient.sql("UPDATE users SET login = :login, role = :role, tenant_id = :tenantId, firstname = :firstname, lastname = :lastname, middlename = :middlename WHERE id = :id")
                .param("login", user.getLogin())
                .param("role", user.getRole())
                .param("tenantId", user.getTenantId() == null ? null : UUID.fromString(user.getTenantId()))
                .param("firstname", user.getFirstname())
                .param("lastname", user.getLastname())
                .param("middlename", user.getMiddlename())
                .param("id", UUID.fromString(user.getId()))
                .update();
    }

    public void create(User user, String rawPassword) {
        jdbcClient.sql("INSERT INTO users (id, login, password, role, firstname, lastname, middlename, tenant_id) VALUES (:id, :login, :password, :role, :firstname, :lastname, :middlename, :tenantId)")
                .param("id", UUID.fromString(user.getId()))
                .param("login", user.getLogin())
                .param("password", PasswordFilter.hash(rawPassword))
                .param("role", user.getRole())
                .param("firstname", user.getFirstname())
                .param("lastname", user.getLastname())
                .param("middlename", user.getMiddlename())
                .param("tenantId", user.getTenantId() == null ? null : UUID.fromString(user.getTenantId()))
                .update();
    }

    public String getPasswordHash(String login) {
        return jdbcClient.sql("SELECT password FROM users WHERE login = :login")
                .param("login", login)
                .query(String.class)
                .optional()
                .orElse(null);
    }

    public void delete(UUID id) {
        jdbcClient.sql("DELETE FROM users WHERE id = :id")
                .param("id", id)
                .update();
    }
}