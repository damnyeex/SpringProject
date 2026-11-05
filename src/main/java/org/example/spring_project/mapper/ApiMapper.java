package org.example.spring_project.mapper;

import org.example.spring_project.dto.*;
import org.example.spring_project.model.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiMapper {

    public UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setRole(user.getRole());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setMiddlename(user.getMiddlename());
        dto.setTenantId(user.getTenantId());
        return dto;
    }

    public List<UserDTO> toUserDTOList(List<User> users) {
        return users.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    public TenantDTO toTenantDTO(Tenant tenant, int userCount) {
        if (tenant == null) return null;
        TenantDTO dto = new TenantDTO();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setDescription(tenant.getDescription());
        dto.setUserCount(userCount);
        return dto;
    }

    public TenantDetailDTO toTenantDetailDTO(Tenant tenant, List<User> users) {
        if (tenant == null) return null;
        TenantDetailDTO dto = new TenantDetailDTO();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setDescription(tenant.getDescription());
        dto.setUserCount(users.size());
        dto.setUsers(toUserDTOList(users));
        return dto;
    }
}