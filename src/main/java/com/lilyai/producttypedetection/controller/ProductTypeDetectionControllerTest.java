package com.lilyai.producttypedetection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilyai.producttypedetection.dto.ProductDetectionRequest;
import com.lilyai.producttypedetection.dto.ProductDetectionResponse;
import com.lilyai.producttypedetection.service.ProductTypeDetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductTypeDetectionController.class)
class ProductTypeDetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductTypeDetectionService productTypeDetectionService;

    @Test
    void detectProductType_ShouldReturnValidResponse_WhenValidRequestProvided() throws Exception {
        // Given
        var request = new ProductDetectionRequest(
            "Apple iPhone 15 Pro Max 256GB Space Black",
            "Latest flagship smartphone with titanium design and advanced camera system",
            List.of("smartphone", "electronics", "mobile"),
            new BigDecimal("1199.99")
        );

        var expectedResponse = new ProductDetectionResponse(
            UUID.randomUUID(),
            "ELECTRONICS",
            "SMARTPHONE",
            new BigDecimal("0.95"),
            List.of(
                new ProductDetectionResponse.CategoryMatch("Electronics", new BigDecimal("0.98")),
                new ProductDetectionResponse.CategoryMatch("Mobile Devices", new BigDecimal("0.92"))
            ),
            Instant.now()
        );

        when(productTypeDetectionService.detectProductType(any(ProductDetectionRequest.class)))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/product-detection/detect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.primaryCategory").value("ELECTRONICS"))
                .andExpect(jsonPath("$.subCategory").value("SMARTPHONE"))
                .andExpect(jsonPath("$.confidenceScore").value(0.95))
                .andExpect(jsonPath("$.categoryMatches").isArray())
                .andExpect(jsonPath("$.categoryMatches[0].category").value("Electronics"))
                .andExpect(jsonPath("$.categoryMatches[0].score").value(0.98))
                .andExpect(jsonPath("$.detectedAt").exists());
    }

    @Test
    void detectProductType_ShouldReturnBadRequest_WhenInvalidRequestProvided() throws Exception {
        // Given
        var invalidRequest = new ProductDetectionRequest(
            "", // Empty title
            null, // Null description
            List.of(),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/product-detection/detect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getDetectionHistory_ShouldReturnPagedResults() throws Exception {
        // Given
        var detectionResults = List.of(
            new ProductDetectionResponse(
                UUID.randomUUID(),
                "ELECTRONICS",
                "SMARTPHONE",
                new BigDecimal("0.95"),
                List.of(),
                Instant.now().minusSeconds(3600)
            ),
            new ProductDetectionResponse(
                UUID.randomUUID(),
                "CLOTHING",
                "SHIRT",
                new BigDecimal("0.88"),
                List.of(),
                Instant.now().minusSeconds(7200)
            )
        );

        when(productTypeDetectionService.getDetectionHistory(0, 10))
            .thenReturn(detectionResults);

        // When & Then
        mockMvc.perform(get("/api/v1/product-detection/history")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].primaryCategory").value("ELECTRONICS"))
                .andExpect(jsonPath("$[1].primaryCategory").value("CLOTHING"));
    }

    @Test
    void getDetectionById_ShouldReturnDetection_WhenValidIdProvided() throws Exception {
        // Given
        var detectionId = UUID.randomUUID();
        var detection = new ProductDetectionResponse(
            detectionId,
            "BOOKS",
            "FICTION",
            new BigDecimal("0.92"),
            List.of(
                new ProductDetectionResponse.CategoryMatch("Literature", new BigDecimal("0.94"))
            ),
            Instant.now()
        );

        when(productTypeDetectionService.getDetectionById(detectionId))
            .thenReturn(detection);

        // When & Then
        mockMvc.perform(get("/api/v1/product-detection/{id}", detectionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(detectionId.toString()))
                .andExpect(jsonPath("$.primaryCategory").value("BOOKS"))
                .andExpect(jsonPath("$.subCategory").value("FICTION"))
                .andExpect(jsonPath("$.confidenceScore").value(0.92));
    }

    @Test
    void getDetectionById_ShouldReturnNotFound_WhenInvalidIdProvided() throws Exception {
        // Given
        var invalidId = UUID.randomUUID();
        when(productTypeDetectionService.getDetectionById(invalidId))
            .thenThrow(new RuntimeException("Detection not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/product-detection/{id}", invalidId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void detectProductType_ShouldReturnUnsupportedMediaType_WhenInvalidContentType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/product-detection/detect")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void detectProductType_ShouldReturnMethodNotAllowed_WhenInvalidHttpMethod() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/product-detection/detect")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }
}