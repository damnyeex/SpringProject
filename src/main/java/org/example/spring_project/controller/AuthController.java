package org.example.spring_project.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.spring_project.model.User;
import org.example.spring_project.repository.UserRepository;
import org.example.spring_project.mapper.ApiMapper;
import org.example.spring_project.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.spring_project.middleware.ResponseMiddleware;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ApiMapper apiMapper;

    public AuthController(UserRepository userRepository, JwtService jwtService, ApiMapper apiMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.apiMapper = apiMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> requestBody) {
        String login = requestBody.get("login");
        String password = requestBody.get("password");
        String role = requestBody.get("role");
        String lastname = requestBody.get("lastname");
        String firstname = requestBody.get("firstname");
        String middlename = requestBody.get("middlename");
        String tenantId = requestBody.get("tenantId");

        if (login == null || login.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "Login and password are required");
        }

        if (userRepository.findByLogin(login) != null) {
            return ResponseMiddleware.error(HttpStatus.CONFLICT, "User with this login already exists");
        }

        String finalRole = (role == null || role.trim().isEmpty()) ? "USER" : role;
        if (!"ADMIN".equals(finalRole) && !"USER".equals(finalRole)) {
            return ResponseMiddleware.error(HttpStatus.BAD_REQUEST, "Invalid role. Allowed: ADMIN, USER");
        }

        User newUser = new User(login, password, finalRole, tenantId, lastname, firstname, middlename);
        newUser.setId(UUID.randomUUID().toString());

        userRepository.create(newUser, password);

        return ResponseMiddleware.created("User registered successfully", apiMapper.toUserDTO(newUser));
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String login = (String) request.getAttribute("login");
        String role = (String) request.getAttribute("role");
        String lastname = (String) request.getAttribute("lastname");
        String firstname = (String) request.getAttribute("firstname");
        String middlename = (String) request.getAttribute("middlename");
        String tenantId = (String) request.getAttribute("tenantId");

        if (userId == null) {
            return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String newToken = jwtService.generateToken(UUID.fromString(userId), login, role, lastname, firstname, middlename, tenantId);

        User tokenUser = new User(login, null, role, tenantId, lastname, firstname, middlename);
        tokenUser.setId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("token", newToken);
        response.put("user", apiMapper.toUserDTO(tokenUser));

        return ResponseMiddleware.success(response);
    }
}
