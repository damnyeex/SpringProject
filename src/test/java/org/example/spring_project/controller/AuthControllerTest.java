package org.example.spring_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_project.dto.request.auth.LoginRequest;
import org.example.spring_project.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest поднимает контроллер, не трогая БД
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // Инструмент для имитации HTTP-запросов

    @Autowired
    private ObjectMapper objectMapper; // Для превращения объекта в JSON

    // сервис, чтобы контроллер не лез в БД
    @MockBean
    private AuthService authService;

    @MockBean
    private org.example.spring_project.repository.UserRepository userRepository;

    @MockBean
    private org.example.spring_project.service.JwtService jwtService;

    @MockBean
    private org.example.spring_project.mapper.ApiMapper apiMapper;

    @Test
    @DisplayName("POST /login возвращает 200 и токен при успехе")
    void shouldReturnTokenOnLoginSuccess() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setLogin("admin");
        request.setPassword("admin123");

        when(authService.login("admin", "admin123")).thenReturn("jwt_token_123");

        org.example.spring_project.model.User mockUser = new org.example.spring_project.model.User();
        mockUser.setRole("ADMIN");
        when(userRepository.findByLogin("admin")).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("jwt_token_123"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /login возвращает 400, если поля пустые")
    void shouldReturnBadRequestOnEmptyFields() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}