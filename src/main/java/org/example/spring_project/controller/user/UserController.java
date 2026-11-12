package org.example.spring_project.controller.user;

import jakarta.validation.Valid;
import org.example.spring_project.model.User;
import org.example.spring_project.repository.UserRepository;
import org.example.spring_project.mapper.ApiMapper;
import org.example.spring_project.security.PasswordFilter;
import org.example.spring_project.middleware.ResponseMiddleware;
import org.example.spring_project.dto.request.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.example.spring_project.security.role.RequireRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ApiMapper apiMapper;

    public UserController(UserRepository userRepository, ApiMapper apiMapper) {
        this.userRepository = userRepository;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ResponseEntity<?> getUsers(HttpServletRequest request) {
        String userIdAttr = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");

        if (userIdAttr == null) return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");

        List<User> users;
        if ("ADMIN".equals(userRole)) {
            users = userRepository.findAll();
        } else {
            User currentUser = userRepository.findById(UUID.fromString(userIdAttr));
            String tenantId = currentUser != null ? currentUser.getTenantId() : null;
            users = (tenantId != null) ? userRepository.findAllByTenantId(tenantId) : List.of();
        }

        return ResponseMiddleware.success(apiMapper.toUserDTOList(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id, HttpServletRequest request) {
        String userIdAttr = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");

        User user = userRepository.findById(id);
        if (user == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "User not found");

        if (!"ADMIN".equals(userRole) && !userIdAttr.equals(user.getId())) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: You can only view your own profile");
        }

        return ResponseMiddleware.success(apiMapper.toUserDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {

        String userIdAttr = (String) httpRequest.getAttribute("userId");
        String userRole = (String) httpRequest.getAttribute("role");

        if (!"ADMIN".equals(userRole) && !userIdAttr.equals(id.toString())) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: You can only update your own profile");
        }

        User existingUser = userRepository.findById(id);
        if (existingUser == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "User not found");

        if (request.getLogin() != null && !request.getLogin().trim().isEmpty()) existingUser.setLogin(request.getLogin());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) existingUser.setPassword(PasswordFilter.hash(request.getPassword()));
        if (request.getLastname() != null && !request.getLastname().trim().isEmpty()) existingUser.setLastname(request.getLastname());
        if (request.getFirstname() != null && !request.getFirstname().trim().isEmpty()) existingUser.setFirstname(request.getFirstname());
        if (request.getMiddlename() != null && !request.getMiddlename().trim().isEmpty()) existingUser.setMiddlename(request.getMiddlename());

        if ("ADMIN".equals(userRole)) {
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                existingUser.setRole(request.getRole());
            }
            if (request.getTenantId() != null) {
                existingUser.setTenantId(request.getTenantId().isEmpty() ? null : request.getTenantId());
            }
        }

        userRepository.update(existingUser);
        return ResponseMiddleware.successMessage("User updated successfully", apiMapper.toUserDTO(existingUser));
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        String userIdAttr = (String) request.getAttribute("userId");

        if (userIdAttr.equals(id.toString())) {
            return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }

        User user = userRepository.findById(id);
        if (user == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "User not found");

        userRepository.delete(id);

        return ResponseMiddleware.successMessage("User deleted successfully", null);
    }
}

