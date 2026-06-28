package com.lovetree.controller;

import com.lovetree.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for PhotoController
 * Covers: /api/photos (GET, POST), /api/photos/{id} (DELETE)
 */
class PhotoControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Upload Photo - Success")
    void uploadPhoto_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/photos")
                .file(file)
                .param("caption", "Our first photo together")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(200))
         .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("url") || response.contains("id"));
    }

    @Test
    @DisplayName("Upload Photo - Without Auth Fails")
    void uploadPhoto_WithoutAuth_Fails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/photos")
                .file(file)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("List Photos - Success")
    void listPhotos_Success() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = authenticatedGet("/api/photos", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records").isArray())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("records"));
    }

    @Test
    @DisplayName("List Photos - With Pagination")
    void listPhotos_WithPagination() throws Exception {
        String token = createCoupleAndGetToken();

        MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/photos")
                .param("page", "1")
                .param("size", "5")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.code").value(200))
         .andReturn();

        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        assertTrue(node.path("data").has("records"));
    }

    @Test
    @DisplayName("Delete Photo - Success")
    void deletePhoto_Success() throws Exception {
        String token = createCoupleAndGetToken();

        // Upload a photo first
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "to-delete.jpg",
            "image/jpeg",
            "content to delete".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/photos")
                .file(file)
                .header("Authorization", "Bearer " + token)
        ).andReturn();

        Long photoId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        // Delete the photo
        authenticatedDelete("/api/photos/" + photoId, token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("Delete Photo - Not Found")
    void deletePhoto_NotFound() throws Exception {
        String token = createCoupleAndGetToken();

        authenticatedDelete("/api/photos/99999", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("Photos - Without Couple Fails")
    void photos_WithoutCouple_Fails() throws Exception {
        String token = registerAndGetToken("nophoto@test.com", "password123", "Solo User");

        authenticatedGet("/api/photos", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }
}
