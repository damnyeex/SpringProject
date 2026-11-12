package org.example.spring_project.controller.tenant;

import jakarta.validation.Valid;
import org.example.spring_project.dto.TenantDTO;
import org.example.spring_project.dto.TenantDetailDTO;
import org.example.spring_project.dto.request.TenantRequest;
import org.example.spring_project.mapper.ApiMapper;
import org.example.spring_project.middleware.ResponseMiddleware;
import org.example.spring_project.model.Tenant;
import org.example.spring_project.repository.TenantRepository;
import org.example.spring_project.security.role.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
    @RequireRole("ADMIN")
    public ResponseEntity<?> getTenants() {
        List<TenantDTO> responseList = new ArrayList<>();
        for (Tenant tenant : tenantRepository.findAll()) {
            int userCount = tenantRepository.getUserCount(tenant.getId());
            responseList.add(apiMapper.toTenantDTO(tenant, userCount));
        }
        return ResponseMiddleware.success(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTenantById(@PathVariable String id) {
        Tenant tenant = tenantRepository.findById(id);
        if (tenant == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");

        TenantDetailDTO detailDTO = apiMapper.toTenantDetailDTO(tenant, tenantRepository.getUsersByTenantId(id));
        return ResponseMiddleware.success(detailDTO);
    }

    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<?> createTenant(@Valid @RequestBody TenantRequest request) {
        if (tenantRepository.findByName(request.getName()) != null) {
            return ResponseMiddleware.error(HttpStatus.CONFLICT, "Tenant with this name already exists");
        }

        Tenant newTenant = new Tenant(request.getName(), request.getDescription());
        newTenant.setId(UUID.randomUUID().toString());
        tenantRepository.create(newTenant);

        return ResponseMiddleware.created("Tenant created successfully", apiMapper.toTenantDTO(newTenant, 0));
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateTenant(@PathVariable String id, @Valid @RequestBody TenantRequest request) {
        Tenant existingTenant = tenantRepository.findById(id);
        if (existingTenant == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");

        existingTenant.setName(request.getName());
        existingTenant.setDescription(request.getDescription());
        tenantRepository.update(existingTenant);

        return ResponseMiddleware.successMessage("Tenant updated successfully", apiMapper.toTenantDTO(existingTenant, tenantRepository.getUserCount(id)));
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteTenant(@PathVariable String id) {
        if (tenantRepository.findById(id) == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "Tenant not found");
        tenantRepository.delete(id);
        return ResponseMiddleware.successMessage("Tenant deleted successfully", null);
    }
}