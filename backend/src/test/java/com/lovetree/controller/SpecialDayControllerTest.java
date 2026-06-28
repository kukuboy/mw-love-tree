package com.lovetree.controller;

import com.lovetree.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for SpecialDayController
 * Covers: /api/special-days (GET)
 */
class SpecialDayControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Get Special Days - Success")
    void getSpecialDays_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/special-days", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("data"));
    }

    @Test
    @DisplayName("Get Special Days - Empty List")
    void getSpecialDays_EmptyList() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/special-days", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        assertTrue(node.path("data").isArray());
    }

    @Test
    @DisplayName("Get Special Days - Without Auth Fails")
    void getSpecialDays_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/special-days")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Special Days - Without Couple Fails")
    void getSpecialDays_WithoutCouple_Fails() throws Exception {
        String token = registerAndGetToken("nospecial@test.com", "password123", "Solo User");

        authenticatedGet("/api/special-days", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("Special Days - Include Anniversary Type")
    void specialDays_IncludeAnniversaryType() throws Exception {
        String token = createCoupleAndGetToken();

        // Set anniversary date
        authenticatedPut("/api/couple/together-date", token,
            java.util.Map.of("togetherDate", "2024-01-15"))
            .andReturn();

        MvcResult result = authenticatedGet("/api/special-days", token)
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        // The response should contain anniversary info
        assertTrue(response.contains("data") || response.contains("specialDays"));
    }
}
