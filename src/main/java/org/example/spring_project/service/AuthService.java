package org.example.spring_project.service;

import org.example.spring_project.model.User;
import org.example.spring_project.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String login(String login, String password) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new RuntimeException("Invalid credentials");
        }

        String hashedPassword = userRepository.getPasswordHash(login);
        if (hashedPassword == null || !BCrypt.checkpw(password, hashedPassword)) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(
                UUID.fromString(user.getId()),
                user.getLogin(),
                user.getRole(),
                user.getLastname(),
                user.getFirstname(),
                user.getMiddlename(),
                user.getTenantId()
        );
    }
}