package org.example.spring_project.controller.tenant;

import org.example.spring_project.dto.TenantDTO;
import org.example.spring_project.dto.TenantDetailDTO;
import org.example.spring_project.model.Tenant;
import org.example.spring_project.model.User;
import org.example.spring_project.repository.TenantRepository;
import org.example.spring_project.mapper.ApiMapper;
import org.example.spring_project.middleware.ResponseMiddleware;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;
    private final ApiMapper apiMapper;

    public TenantController(TenantRepository tenantRepository, ApiMapper apiMapper) {
        this.tenantRepository = tenantRepository;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ResponseEntity<?> getTenants(HttpServletRequest request) {
        if (request.getAttribute("userId") == null) return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");

        List<Tenant> tenants = tenantRepository.findAll();
        List<TenantDTO> responseList = new ArrayList<>();

        for (Tenant tenant : tenants) {
            int userCount = tenantRepository.getUserCount(tenant.getId());
            responseList.add(apiMapper.toTenantDTO(tenant, userCount));
        }

        return ResponseMiddleware.success(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTenantById(@PathVariable String id, HttpServletRequest request) {
        if (request.getAttribute("userId") == null) return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");

        Tenant tenant = tenantRepository.findById(id);
        if (tenant == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");

        List<User> users = tenantRepository.getUsersByTenantId(id);

        TenantDetailDTO detailDTO = apiMapper.toTenantDetailDTO(tenant, users);

        return ResponseMiddleware.success(detailDTO);
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

        return ResponseMiddleware.created("Tenant created successfully", apiMapper.toTenantDTO(newTenant, 0));
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

        int userCount = tenantRepository.getUserCount(id);
        return ResponseMiddleware.successMessage("Tenant updated successfully", apiMapper.toTenantDTO(existingTenant, userCount));
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