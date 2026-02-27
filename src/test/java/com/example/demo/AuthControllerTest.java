package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Auth API endpoints.
 *
 * Uses an H2 in-memory database (application-test.properties) so no
 * MySQL server is required during testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Registration ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register → 201 CREATED with JWT token")
    void register_validRequest_returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice Test", "alice@test.com", "password123", "MIT");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.role").value("PARTICIPANT"));
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when email already registered")
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Bob Dup", "dup@test.com", "password123", "IIT");

        // First registration — succeeds
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email — must fail
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("already registered")));
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when name is blank")
    void register_blankName_returns400WithValidationError() throws Exception {
        RegisterRequest request = new RegisterRequest("", "valid@test.com", "password123", null);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when password too short")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Charlie", "charlie@test.com", "123", null);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register → role is always PARTICIPANT (M4 security fix)")
    void register_doesNotAllowRoleEscalation() throws Exception {
        // Even if a client tries to inject role, it should be ignored
        // because RegisterRequest no longer has a role field.
        String rawJson = """
                {
                  "name": "Hacker",
                  "email": "hacker@test.com",
                  "password": "password123",
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawJson))
                .andExpect(status().isCreated())
                // Role MUST be PARTICIPANT regardless of input
                .andExpect(jsonPath("$.role").value("PARTICIPANT"));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login → 200 OK with valid credentials")
    void login_validCredentials_returns200AndToken() throws Exception {
        // Register first
        RegisterRequest reg = new RegisterRequest("Dave", "dave@test.com", "secure123", "NIT");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest login = new LoginRequest("dave@test.com", "secure123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("dave@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login → 400 with wrong password")
    void login_wrongPassword_returns400() throws Exception {
        RegisterRequest reg = new RegisterRequest("Eve", "eve@test.com", "correct123", "IIM");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("eve@test.com", "wrong_password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login → 400 with non-existent user")
    void login_nonExistentUser_returns400() throws Exception {
        LoginRequest login = new LoginRequest("nobody@nowhere.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }
}
