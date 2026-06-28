package com.lovetree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Base test class for all controller tests.
 * Provides common utilities for authentication and API testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Register a new user and return the auth token.
     */
    protected String registerAndGetToken(String email, String password, String nickname) throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of(
                "email", email,
                "password", password,
                "nickname", nickname
            )
        );

        MvcResult result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        return node.path("data").path("token").asText();
    }

    /**
     * Login and return the auth token.
     */
    protected String loginAndGetToken(String email, String password) throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of(
                "email", email,
                "password", password
            )
        );

        MvcResult result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        return node.path("data").path("token").asText();
    }

    /**
     * Create a couple with two users and return the auth token of user1.
     */
    protected String createCoupleAndGetToken() throws Exception {
        // Register two users
        String token1 = registerAndGetToken("user1@test.com", "password123", "User One");
        String token2 = registerAndGetToken("user2@test.com", "password123", "User Two");

        // Get invite code for user1
        MvcResult result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/couple/invite")
                .header("Authorization", "Bearer " + token1)
        ).andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        String inviteCode = node.path("data").path("inviteCode").asText();

        // User2 joins the couple
        String joinBody = objectMapper.writeValueAsString(
            java.util.Map.of("inviteCode", inviteCode)
        );

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/couple/join")
                .header("Authorization", "Bearer " + token2)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(joinBody)
        ).andReturn();

        return token1;
    }

    /**
     * Make an authenticated GET request.
     */
    protected MvcResult authenticatedGet(String url, String token) throws Exception {
        return mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get(url)
                .header("Authorization", "Bearer " + token)
        ).andReturn();
    }

    /**
     * Make an authenticated POST request.
     */
    protected MvcResult authenticatedPost(String url, String token, Object body) throws Exception {
        return mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andReturn();
    }

    /**
     * Make an authenticated PUT request.
     */
    protected MvcResult authenticatedPut(String url, String token, Object body) throws Exception {
        return mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andReturn();
    }

    /**
     * Make an authenticated DELETE request.
     */
    protected MvcResult authenticatedDelete(String url, String token) throws Exception {
        return mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete(url)
                .header("Authorization", "Bearer " + token)
        ).andReturn();
    }
}
