package org.example.spring_project.dto;

public class TenantDTO {
    private String id;
    private String name;
    private String description;
    private int userCount;

    // геттеры/сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getUserCount() { return userCount; }
    public void setUserCount(int userCount) { this.userCount = userCount; }
}
