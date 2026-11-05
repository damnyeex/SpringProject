package org.example.spring_project.controller.user;

import org.example.spring_project.model.User;
import org.example.spring_project.repository.UserRepository;
import org.example.spring_project.mapper.ApiMapper;
import org.example.spring_project.security.PasswordFilter;
import org.example.spring_project.middleware.ResponseMiddleware;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        String userIdAttr = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");

        UUID targetUserId;
        try {
            targetUserId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "Invalid user ID format");
        }

        User user = userRepository.findById(targetUserId);
        if (user == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "User not found");

        if (!"ADMIN".equals(userRole) && !userIdAttr.equals(user.getId())) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: You can only view your own profile");
        }

        return ResponseMiddleware.success(apiMapper.toUserDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        String userIdAttr = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");

        UUID targetUserId = UUID.fromString(id);
        if (!"ADMIN".equals(userRole) && !userIdAttr.equals(targetUserId.toString())) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: You can only update your own profile");
        }

        User existingUser = userRepository.findById(targetUserId);
        if (existingUser == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "User not found");

        String newLogin = (String) body.get("login");
        String newPassword = (String) body.get("password");
        String newLastname = (String) body.get("lastname");
        String newFirstname = (String) body.get("firstname");
        String newMiddlename = (String) body.get("middlename");
        String newRole = (String) body.get("role");
        String newTenantId = (String) body.get("tenantId");

        if (!"ADMIN".equals(userRole)) {
            newRole = existingUser.getRole();
            newTenantId = existingUser.getTenantId();
        }

        if (newLogin != null && !newLogin.trim().isEmpty()) existingUser.setLogin(newLogin);
        if (newPassword != null && !newPassword.trim().isEmpty()) existingUser.setPassword(PasswordFilter.hash(newPassword));
        if (newLastname != null && !newLastname.trim().isEmpty()) existingUser.setLastname(newLastname);
        if (newFirstname != null && !newFirstname.trim().isEmpty()) existingUser.setFirstname(newFirstname);
        if (newMiddlename != null && !newMiddlename.trim().isEmpty()) existingUser.setMiddlename(newMiddlename);

        if (newRole != null && !newRole.trim().isEmpty()) {
            if (!"ADMIN".equals(newRole) && !"USER".equals(newRole)) {
                return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "Invalid role");
            }
            existingUser.setRole(newRole);
        }
        if (newTenantId != null) {
            existingUser.setTenantId(newTenantId.isEmpty() ? null : newTenantId);
        }

        userRepository.update(existingUser);
        return ResponseMiddleware.successMessage("User updated successfully", apiMapper.toUserDTO(existingUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, HttpServletRequest request) {
        String userIdAttr = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");

        if (!"ADMIN".equals(userRole)) {
            return ResponseMiddleware.error(HttpStatus.FORBIDDEN, "Access denied: Only administrators can delete users");
        }

        if (userIdAttr.equals(id)) {
            return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }

        User user = userRepository.findById(UUID.fromString(id));
        if (user == null) return ResponseMiddleware.error(HttpStatus.NOT_FOUND, "User not found");

        userRepository.delete(UUID.fromString(id)); // Теперь метод существует

        return ResponseMiddleware.successMessage("User deleted successfully", null);
    }
}
