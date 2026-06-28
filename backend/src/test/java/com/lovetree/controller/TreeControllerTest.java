package com.lovetree.controller;

import com.lovetree.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for TreeController
 * Covers: /api/tree (GET), /api/tree/stats (GET)
 */
class TreeControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Get Tree - Success")
    void getTree_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/tree", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("data"));
    }

    @Test
    @DisplayName("Get Tree - Contains Growth Info")
    void getTree_ContainsGrowthInfo() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/tree", token)
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        // Tree should have level and growth
        assertTrue(node.path("data").has("level") || response.contains("growth"));
    }

    @Test
    @DisplayName("Get Tree - Without Auth Fails")
    void getTree_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/tree")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Tree - Without Couple Fails")
    void getTree_WithoutCouple_Fails() throws Exception {
        String token = registerAndGetToken("notree@test.com", "password123", "Solo User");

        authenticatedGet("/api/tree", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("Get Tree Stats - Success")
    void getTreeStats_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/tree/stats", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("data"));
    }

    @Test
    @DisplayName("Get Tree Stats - Contains Statistics")
    void getTreeStats_ContainsStatistics() throws Exception {
        String token = createCoupleAndGetToken();

        // Create some events
        authenticatedPost("/api/events", token,
            java.util.Map.of("title", "Test Event", "eventDate", "2024-02-14"))
            .andReturn();

        // Send some messages
        authenticatedPost("/api/messages", token,
            java.util.Map.of("content", "Test message"))
            .andReturn();

        MvcResult result = authenticatedGet("/api/tree/stats", token)
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        // Stats should have various counts
        assertTrue(
            node.path("data").has("totalEvents") ||
            node.path("data").has("totalMessages") ||
            node.path("data").has("daysTogether")
        );
    }

    @Test
    @DisplayName("Get Tree Stats - Without Auth Fails")
    void getTreeStats_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/tree/stats")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Tree Stats - Without Couple Fails")
    void getTreeStats_WithoutCouple_Fails() throws Exception {
        String token = registerAndGetToken("nostats@test.com", "password123", "Solo User");

        authenticatedGet("/api/tree/stats", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }
}
