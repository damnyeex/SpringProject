package org.example.spring_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String login;
    private String role;
    private String firstname;
    private String lastname;
    private String middlename;
    private String tenantId;
}