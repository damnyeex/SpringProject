package org.example.spring_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TenantDetailDTO {
    private String id;
    private String name;
    private String description;
    private int userCount;
    private List<UserDTO> users;
}


