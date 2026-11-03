package org.example.spring_project.controller.tenant;

import org.example.spring_project.model.Tenant;
import org.example.spring_project.model.User;
import org.example.spring_project.repository.TenantRepository;
import org.example.spring_project.middleware.ResponseMiddleware;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    public ResponseEntity<?> getTenants(HttpServletRequest request) {
        if (request.getAttribute("userId") == null) return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");

        List<Tenant> tenants = tenantRepository.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Tenant tenant : tenants) {
            Map<String, Object> tenantInfo = new HashMap<>();
            tenantInfo.put("id", tenant.getId());
            tenantInfo.put("name", tenant.getName());
            tenantInfo.put("description", tenant.getDescription());
            tenantInfo.put("userCount", tenantRepository.getUserCount(tenant.getId()));
            responseList.add(tenantInfo);
        }
        return ResponseMiddleware.success(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTenantById(@PathVariable String id, HttpServletRequest request) {
        if (request.getAttribute("userId") == null) return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");

        Tenant tenant = tenantRepository.findById(id);
        if (tenant == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");

        List<User> users = tenantRepository.getUsersByTenantId(id);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", tenant.getId());
        responseData.put("name", tenant.getName());
        responseData.put("description", tenant.getDescription());
        responseData.put("userCount", users.size());
        responseData.put("users", users);

        return ResponseMiddleware.success(responseData);
    }

    @PostMapping
    public ResponseEntity<?> createTenant(@RequestBody Map<String, String> body, HttpServletRequest request) {
        if (!"ADMIN".equals(request.getAttribute("role"))) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: Only administrators can create tenants");
        }

        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "Tenant name is required");
        }

        Tenant newTenant = new Tenant(name, body.get("description"));
        newTenant.setId(UUID.randomUUID().toString());
        tenantRepository.create(newTenant);

        return ResponseMiddleware.created("Tenant created successfully", newTenant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTenant(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        if (!"ADMIN".equals(request.getAttribute("role"))) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: Only administrators can update tenants");
        }

        Tenant existingTenant = tenantRepository.findById(id);
        if (existingTenant == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");

        String name = body.get("name");
        String description = body.get("description");

        if (name != null && !name.trim().isEmpty()) existingTenant.setName(name);
        if (description != null) existingTenant.setDescription(description);

        tenantRepository.update(existingTenant);
        return ResponseMiddleware.successMessage("Tenant updated successfully", existingTenant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTenant(@PathVariable String id, HttpServletRequest request) {
        if (!"ADMIN".equals(request.getAttribute("role"))) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: Only administrators can delete tenants");
        }

        Tenant tenant = tenantRepository.findById(id);
        if (tenant == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");

        tenantRepository.delete(id);
        return ResponseMiddleware.successMessage("Tenant deleted successfully", null);
    }
}