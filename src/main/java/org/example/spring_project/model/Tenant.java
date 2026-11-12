package org.example.spring_project.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Tenant {
    private String id;
    private String name;
    private String description;

    public Tenant(String name, String description) {
        this.name = name;
        this.description = description;
    }
}