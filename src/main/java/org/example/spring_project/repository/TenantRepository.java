package org.example.spring_project.repository;

import org.example.spring_project.model.Tenant;
import org.example.spring_project.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TenantRepository {

    private final JdbcClient jdbcClient;
    private final UserRepository userRepository; // для делегирования

    public TenantRepository(JdbcClient jdbcClient, UserRepository userRepository) {
        this.jdbcClient = jdbcClient;
        this.userRepository = userRepository;
    }

    private static final RowMapper<Tenant> TENANT_ROW_MAPPER = (rs, rowNum) -> {
        Tenant t = new Tenant(rs.getString("name"), rs.getString("description"));
        t.setId(rs.getObject("id").toString());
        return t;
    };

    public List<Tenant> findAll() {
        return jdbcClient.sql("SELECT id, name, description FROM tenants")
                .query(TENANT_ROW_MAPPER)
                .list();
    }

    public Tenant findById(String id) {
        return jdbcClient.sql("SELECT id, name, description FROM tenants WHERE id = :id")
                .param("id", UUID.fromString(id))
                .query(TENANT_ROW_MAPPER)
                .optional()
                .orElse(null);
    }

    public void create(Tenant tenant) {
        jdbcClient.sql("INSERT INTO tenants (id, name, description) VALUES (:id, :name, :description)")
                .param("id", UUID.fromString(tenant.getId()))
                .param("name", tenant.getName())
                .param("description", tenant.getDescription())
                .update();
    }

    public void update(Tenant tenant) {
        jdbcClient.sql("UPDATE tenants SET name = :name, description = :description WHERE id = :id")
                .param("name", tenant.getName())
                .param("description", tenant.getDescription())
                .param("id", UUID.fromString(tenant.getId()))
                .update();
    }

    public void delete(String id) {
        jdbcClient.sql("DELETE FROM tenants WHERE id = :id")
                .param("id", UUID.fromString(id))
                .update();
    }

    public int getUserCount(String tenantId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM users WHERE tenant_id = :tenantId")
                .param("tenantId", UUID.fromString(tenantId))
                .query(Integer.class)
                .single();
    }

    public List<User> getUsersByTenantId(String tenantId) {
        // Твоя исходная логика: вызов метода из UserRepository
        return userRepository.findAllByTenantId(tenantId);
    }
}