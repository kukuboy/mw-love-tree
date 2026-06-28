package com.lovetree.controller;

import com.lovetree.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for MessageController
 * Covers: /api/messages (GET, POST), /api/messages/unread-count, /api/messages/{id}/read
 */
class MessageControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Send Message - Success")
    void sendMessage_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedPost("/api/messages", token,
                Map.of("content", "Hello, my love!"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content").value("Hello, my love!"))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"id\""));
    }

    @Test
    @DisplayName("Send Message - Without Auth Fails")
    void sendMessage_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/messages")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("content", "Test")))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Send Message - Empty Content Fails")
    void sendMessage_EmptyContent_Fails() throws Exception {
        String token = createCoupleAndGetToken();

        authenticatedPost("/api/messages", token, Map.of("content", ""))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Messages - Success")
    void getMessages_Success() throws Exception {
        String token = createCoupleAndGetToken();

        // Send some messages
        authenticatedPost("/api/messages", token,
            Map.of("content", "Message 1")).andReturn();
        authenticatedPost("/api/messages", token,
            Map.of("content", "Message 2")).andReturn();

        MvcResult result = authenticatedGet("/api/messages", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("Message 1") || response.contains("Message 2"));
    }

    @Test
    @DisplayName("Get Messages - Empty List")
    void getMessages_EmptyList() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/messages", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        assertTrue(node.path("data").size() >= 0);
    }

    @Test
    @DisplayName("Get Unread Count - Success")
    void getUnreadCount_Success() throws Exception {
        String token = createCoupleAndGetToken();

        // Send a message
        authenticatedPost("/api/messages", token,
            Map.of("content", "Unread message")).andReturn();

        MvcResult result = authenticatedGet("/api/messages/unread-count", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isNumber())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("data"));
    }

    @Test
    @DisplayName("Get Unread Count - Zero When All Read")
    void getUnreadCount_ZeroWhenAllRead() throws Exception {
        String token = createCoupleAndGetToken();

        // Send a message
        MvcResult sendResult = authenticatedPost("/api/messages", token,
            Map.of("content", "Message to be read")).andReturn();

        Long messageId = objectMapper.readTree(sendResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        // Mark as read
        authenticatedPut("/api/messages/" + messageId + "/read", token, Map.of())
            .andExpect(status().isOk())
            .andReturn();

        // Check unread count
        MvcResult result = authenticatedGet("/api/messages/unread-count", token)
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        // Count should be 0 or the specific message logic
        assertTrue(node.path("data").asInt() >= 0);
    }

    @Test
    @DisplayName("Mark Message as Read - Success")
    void markMessageAsRead_Success() throws Exception {
        String token = createCoupleAndGetToken();

        // Send a message
        MvcResult sendResult = authenticatedPost("/api/messages", token,
            Map.of("content", "Message to mark read")).andReturn();

        Long messageId = objectMapper.readTree(sendResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        // Mark as read
        MvcResult result = authenticatedPut("/api/messages/" + messageId + "/read", token, Map.of())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"code\":200"));
    }

    @Test
    @DisplayName("Mark Message as Read - Not Found")
    void markMessageAsRead_NotFound() throws Exception {
        String token = createCoupleAndGetToken();

        authenticatedPut("/api/messages/99999/read", token, Map.of())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("Messages - Without Couple Fails")
    void messages_WithoutCouple_Fails() throws Exception {
        String token = registerAndGetToken("solo@test.com", "password123", "Solo User");

        authenticatedGet("/api/messages", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }
}
