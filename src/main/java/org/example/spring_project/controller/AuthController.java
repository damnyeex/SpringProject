package org.example.spring_project.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.spring_project.dto.request.RegisterRequest;
import org.example.spring_project.mapper.ApiMapper;
import org.example.spring_project.middleware.ResponseMiddleware;
import org.example.spring_project.model.User;
import org.example.spring_project.repository.UserRepository;
import org.example.spring_project.security.RequireRole;
import org.example.spring_project.service.AuthService;
import org.example.spring_project.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthService authService;
    private final ApiMapper apiMapper;

    public AuthController(UserRepository userRepository, JwtService jwtService, AuthService authService, ApiMapper apiMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authService = authService;
        this.apiMapper = apiMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody, HttpServletResponse response) {
        try {
            String token = authService.login(requestBody.get("login"), requestBody.get("password"));
            User user = userRepository.findByLogin(requestBody.get("login"));

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("role", user.getRole());
            res.put("token", token);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @RequireRole("ADMIN")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByLogin(request.getLogin()) != null) {
            return ResponseMiddleware.error(HttpStatus.CONFLICT, "User with this login already exists");
        }

        String finalRole = (request.getRole() == null || request.getRole().trim().isEmpty()) ? "USER" : request.getRole();

        User newUser = new User(
                request.getLogin(), request.getPassword(), finalRole, request.getTenantId(),
                request.getLastname(), request.getFirstname(), request.getMiddlename()
        );
        newUser.setId(UUID.randomUUID().toString());
        userRepository.create(newUser, request.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", apiMapper.toUserDTO(newUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseMiddleware.error(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String login = (String) request.getAttribute("login");
        String role = (String) request.getAttribute("role");
        String lastname = (String) request.getAttribute("lastname");
        String firstname = (String) request.getAttribute("firstname");
        String middlename = (String) request.getAttribute("middlename");
        String tenantId = (String) request.getAttribute("tenantId");

        String newToken = jwtService.generateToken(UUID.fromString(userId), login, role, lastname, firstname, middlename, tenantId);

        User tokenUser = new User(login, null, role, tenantId, lastname, firstname, middlename);
        tokenUser.setId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("token", newToken);
        response.put("user", apiMapper.toUserDTO(tokenUser));

        return ResponseEntity.ok(response);
    }
}