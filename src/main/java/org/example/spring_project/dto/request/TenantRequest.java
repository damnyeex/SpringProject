package org.example.spring_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantRequest {
    @NotBlank(message = "Tenant name cannot be empty")
    private String name;
    private String description;
}