package com.lovetree.controller;

import com.lovetree.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for AuthController
 * Covers: /api/auth/register, /api/auth/login
 */
class AuthControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Register - Success")
    void register_Success() throws Exception {
        String email = "newuser" + System.currentTimeMillis() + "@test.com";
        String requestBody = """
            {
                "email": "%s",
                "password": "password123",
                "nickname": "Test User"
            }
            """.formatted(email);

        MvcResult result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(200))
         .andExpect(jsonPath("$.data.token").isNotEmpty())
         .andExpect(jsonPath("$.data.userId").isNumber())
         .andExpect(jsonPath("$.data.nickname").value("Test User"))
         .andExpect(jsonPath("$.data.coupleId").isEmpty())
         .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"token\""));
        assertTrue(response.contains("\"userId\""));
    }

    @Test
    @DisplayName("Register - Duplicate Email Fails")
    void register_DuplicateEmail_Fails() throws Exception {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";
        String requestBody = """
            {
                "email": "%s",
                "password": "password123",
                "nickname": "User One"
            }
            """.formatted(email);

        // First registration should succeed
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isOk());

        // Second registration with same email should fail
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("Register - Invalid Email Format Fails")
    void register_InvalidEmail_Fails() throws Exception {
        String requestBody = """
            {
                "email": "invalid-email",
                "password": "password123",
                "nickname": "Test User"
            }
            """;

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Empty Password Fails")
    void register_EmptyPassword_Fails() throws Exception {
        String requestBody = """
            {
                "email": "test@test.com",
                "password": "",
                "nickname": "Test User"
            }
            """;

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() throws Exception {
        // Register first
        String email = "logintest" + System.currentTimeMillis() + "@test.com";
        String password = "password123";
        registerAndGetToken(email, password, "Login Tester");

        String requestBody = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        MvcResult result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(200))
         .andExpect(jsonPath("$.data.token").isNotEmpty())
         .andExpect(jsonPath("$.data.userId").isNumber())
         .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"token\""));
    }

    @Test
    @DisplayName("Login - Wrong Password Fails")
    void login_WrongPassword_Fails() throws Exception {
        // Register first
        String email = "wrongpw" + System.currentTimeMillis() + "@test.com";
        registerAndGetToken(email, "correctpassword", "Test User");

        String requestBody = """
            {
                "email": "%s",
                "password": "wrongpassword"
            }
            """.formatted(email);

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("Login - Non-existent User Fails")
    void login_NonExistentUser_Fails() throws Exception {
        String requestBody = """
            {
                "email": "nonexistent@test.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(401));
    }
}
