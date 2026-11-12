package org.example.spring_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TenantDTO {
    private String id;
    private String name;
    private String description;
    private int userCount;
}
