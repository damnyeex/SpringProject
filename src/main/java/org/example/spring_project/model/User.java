package org.example.spring_project.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
    private String id;
    private String login;
    private String password;
    private String role;
    private String firstname;
    private String lastname;
    private String middlename;
    private String tenantId;

    public User(String login, String password, String role, String tenantId,
                String lastname, String firstname, String middlename) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.tenantId = tenantId;
        this.lastname = lastname;
        this.firstname = firstname;
        this.middlename = middlename;
    }
}