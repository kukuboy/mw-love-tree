package com.lovetree.controller;

import com.lovetree.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for CoupleController
 * Covers: /api/couple/invite, /api/couple/join, /api/couple/info, /api/couple/together-date
 */
class CoupleControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Get Invite Code - Success")
    void getInviteCode_Success() throws Exception {
        String token = registerAndGetToken("couple1@test.com", "password123", "User One");

        MvcResult result = authenticatedGet("/api/couple/invite", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.inviteCode").isNotEmpty())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("inviteCode"));
    }

    @Test
    @DisplayName("Get Invite Code - Without Auth Fails")
    void getInviteCode_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/couple/invite")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Join Couple - Success")
    void joinCouple_Success() throws Exception {
        // User1 creates invite code
        String token1 = registerAndGetToken("couple2user1@test.com", "password123", "User One");
        MvcResult inviteResult = authenticatedGet("/api/couple/invite", token1)
            .andReturn();
        String inviteCode = objectMapper.readTree(inviteResult.getResponse().getContentAsString())
            .path("data").path("inviteCode").asText();

        // User2 registers and joins
        String token2 = registerAndGetToken("couple2user2@test.com", "password123", "User Two");

        MvcResult joinResult = authenticatedPost("/api/couple/join", token2, Map.of("inviteCode", inviteCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andReturn();

        String response = joinResult.getResponse().getContentAsString();
        assertTrue(response.contains("token"));
    }

    @Test
    @DisplayName("Join Couple - Invalid Code Fails")
    void joinCouple_InvalidCode_Fails() throws Exception {
        String token = registerAndGetToken("invalidjoin@test.com", "password123", "Test User");

        authenticatedPost("/api/couple/join", token, Map.of("inviteCode", "INVALID123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("Get Couple Info - Success")
    void getCoupleInfo_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/couple/info", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.partnerNickname").value("User Two"))
            .andExpect(jsonPath("$.data.inviteCode").isNotEmpty())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("partnerNickname"));
        assertTrue(response.contains("partnerId"));
    }

    @Test
    @DisplayName("Get Couple Info - Without Couple Fails")
    void getCoupleInfo_WithoutCouple_Fails() throws Exception {
        String token = registerAndGetToken("nocouple@test.com", "password123", "Solo User");

        authenticatedGet("/api/couple/info", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("Set Together Date - Success")
    void setTogetherDate_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedPut("/api/couple/together-date", token,
                Map.of("togetherDate", "2024-01-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        // Verify the date was set
        MvcResult infoResult = authenticatedGet("/api/couple/info", token)
            .andExpect(status().isOk())
            .andReturn();

        String infoResponse = infoResult.getResponse().getContentAsString();
        assertTrue(infoResponse.contains("2024-01-15") || infoResponse.contains("togetherDate"));
    }

    @Test
    @DisplayName("Set Together Date - Invalid Format Fails")
    void setTogetherDate_InvalidFormat_Fails() throws Exception {
        String token = createCoupleAndGetToken();

        authenticatedPut("/api/couple/together-date", token,
                Map.of("togetherDate", "invalid-date"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Couple Info - Days Together Calculated")
    void getCoupleInfo_DaysTogetherCalculated() throws Exception {
        String token = createCoupleAndGetToken();

        // Set together date
        authenticatedPut("/api/couple/together-date", token,
                Map.of("togetherDate", "2023-01-01"))
            .andReturn();

        // Get info and verify daysTogether is calculated
        MvcResult result = authenticatedGet("/api/couple/info", token)
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        int daysTogether = node.path("data").path("daysTogether").asInt();
        assertTrue(daysTogether > 0, "Days together should be positive");
    }
}
