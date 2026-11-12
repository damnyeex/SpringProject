package org.example.spring_project.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;

    @Pattern(regexp = "^$|.{5,}", message = "Password must be empty or at least 5 characters")
    private String password;

    private String firstname;
    private String lastname;
    private String middlename;

    @Pattern(regexp = "^$|ADMIN|USER", message = "Role must be ADMIN, USER or empty")
    private String role;

    private String tenantId;
}