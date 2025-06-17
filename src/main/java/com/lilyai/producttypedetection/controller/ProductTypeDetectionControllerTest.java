package com.lilyai.producttypedetection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilyai.producttypedetection.dto.ProductDetectionRequest;
import com.lilyai.producttypedetection.dto.ProductDetectionResponse;
import com.lilyai.producttypedetection.dto.ProductIngestionRequest;
import com.lilyai.producttypedetection.dto.ProductIngestionResponse;
import com.lilyai.producttypedetection.service.ProductTypeDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductTypeDetectionController.class)
@DisplayName("Product Type Detection Controller Tests")
class ProductTypeDetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductTypeDetectionService detectionService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDetectionRequest validDetectionRequest;
    private ProductIngestionRequest validIngestionRequest;
    private ProductDetectionResponse mockDetectionResponse;
    private ProductIngestionResponse mockIngestionResponse;

    @BeforeEach
    void setUp() {
        validDetectionRequest = new ProductDetectionRequest(
            "iPhone 15 Pro Max 256GB Space Black",
            "Latest Apple smartphone with titanium design",
            "https://example.com/iphone15.jpg",
            Map.of(
                "brand", "Apple",
                "storage", "256GB",
                "color", "Space Black"
            )
        );

        validIngestionRequest = new ProductIngestionRequest(
            List.of(
                Map.of(
                    "name", "Samsung Galaxy S24",
                    "description", "Android flagship smartphone",
                    "category", "Electronics"
                ),
                Map.of(
                    "name", "Nike Air Max 90",
                    "description", "Classic running shoes",
                    "category", "Footwear"
                )
            )
        );

        mockDetectionResponse = new ProductDetectionResponse(
            "ELECTRONICS",
            "SMARTPHONE",
            0.95,
            Map.of(
                "brand_detected", "Apple",
                "model_series", "iPhone",
                "generation", "15"
            )
        );

        mockIngestionResponse = new ProductIngestionResponse(
            2,
            2,
            0,
            List.of(
                "PRD-001", "PRD-002"
            ),
            List.of()
        );
    }

    @Nested
    @DisplayName("Detection API Tests")
    class DetectionApiTests {

        @Test
        @DisplayName("Should successfully detect product type with valid request")
        void shouldDetectProductTypeSuccessfully() throws Exception {
            // Given
            when(detectionService.detectProductType(any(ProductDetectionRequest.class)))
                .thenReturn(mockDetectionResponse);

            // When & Then
            performDetectionRequest(validDetectionRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.primaryCategory").value("ELECTRONICS"))
                .andExpect(jsonPath("$.subCategory").value("SMARTPHONE"))
                .andExpect(jsonPath("$.confidence").value(0.95))
                .andExpect(jsonPath("$.attributes.brand_detected").value("Apple"));

            verify(detectionService, times(1)).detectProductType(any(ProductDetectionRequest.class));
        }

        @Test
        @DisplayName("Should return 400 for request with null product name")
        void shouldReturn400ForNullProductName() throws Exception {
            // Given
            var invalidRequest = new ProductDetectionRequest(
                null,
                "Valid description",
                "https://example.com/image.jpg",
                Map.of()
            );

            // When & Then
            performDetectionRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());

            verify(detectionService, never()).detectProductType(any());
        }

        @Test
        @DisplayName("Should return 400 for request with empty product name")
        void shouldReturn400ForEmptyProductName() throws Exception {
            // Given
            var invalidRequest = new ProductDetectionRequest(
                "",
                "Valid description",
                "https://example.com/image.jpg",
                Map.of()
            );

            // When & Then
            performDetectionRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

            verify(detectionService, never()).detectProductType(any());
        }

        @Test
        @DisplayName("Should return 400 for request with invalid image URL")
        void shouldReturn400ForInvalidImageUrl() throws Exception {
            // Given
            var invalidRequest = new ProductDetectionRequest(
                "Valid Product Name",
                "Valid description",
                "invalid-url",
                Map.of()
            );

            // When & Then
            performDetectionRequest(invalidRequest)
                .andExpected(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid URL format")));
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void shouldReturn500WhenServiceThrowsException() throws Exception {
            // Given
            when(detectionService.detectProductType(any(ProductDetectionRequest.class)))
                .thenThrow(new RuntimeException("Internal service error"));

            // When & Then
            performDetectionRequest(validDetectionRequest)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 400 for malformed JSON request")
        void shouldReturn400ForMalformedJson() throws Exception {
            // Given
            String malformedJson = "{\"name\": \"Product\", \"description\": }";

            // When & Then
            mockMvc.perform(post("/api/v1/detect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_JSON"));
        }

        private ResultActions performDetectionRequest(ProductDetectionRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/detect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @DisplayName("Ingestion API Tests")
    class IngestionApiTests {

        @Test
        @DisplayName("Should successfully ingest products with valid request")
        void shouldIngestProductsSuccessfully() throws Exception {
            // Given
            when(detectionService.ingestProducts(any(ProductIngestionRequest.class)))
                .thenReturn(mockIngestionResponse);

            // When & Then
            performIngestionRequest(validIngestionRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfullyIngested").value(2))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.productIds").isArray())
                .andExpect(jsonPath("$.productIds.length()").value(2));

            verify(detectionService, times(1)).ingestProducts(any(ProductIngestionRequest.class));
        }

        @Test
        @DisplayName("Should return 400 for empty products list")
        void shouldReturn400ForEmptyProductsList() throws Exception {
            // Given
            var invalidRequest = new ProductIngestionRequest(List.of());

            // When & Then
            performIngestionRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Products list cannot be empty")));

            verify(detectionService, never()).ingestProducts(any());
        }

        @Test
        @DisplayName("Should return 400 for null products list")
        void shouldReturn400ForNullProductsList() throws Exception {
            // Given
            var invalidRequest = new ProductIngestionRequest(null);

            // When & Then
            performIngestionRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

            verify(detectionService, never()).ingestProducts(any());
        }

        @Test
        @DisplayName("Should return 413 for request exceeding size limit")
        void shouldReturn413ForOversizedRequest() throws Exception {
            // Given - Create a large request with many products
            var largeProductsList = java.util.stream.IntStream.range(0, 1001)
                .mapToObj(i -> Map.of(
                    "name", "Product " + i,
                    "description", "Description for product " + i,
                    "category", "Category"
                ))
                .toList();
            
            var oversizedRequest = new ProductIngestionRequest(largeProductsList);

            // When & Then
            performIngestionRequest(oversizedRequest)
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value("PAYLOAD_TOO_LARGE"))
                .andExpect(jsonPath("$.message").value(containsString("Request exceeds maximum allowed size")));
        }

        @Test
        @DisplayName("Should return partial success response when some products fail")
        void shouldReturnPartialSuccessResponse() throws Exception {
            // Given
            var partialSuccessResponse = new ProductIngestionResponse(
                2,
                1,
                1,
                List.of("PRD-001"),
                List.of("Product 2: Invalid category")
            );
            
            when(detectionService.ingestProducts(any(ProductIngestionRequest.class)))
                .thenReturn(partialSuccessResponse);

            // When & Then
            performIngestionRequest(validIngestionRequest)
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfullyIngested").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(1));
        }

        @Test
        @DisplayName("Should return 500 when ingestion service throws exception")
        void shouldReturn500WhenIngestionServiceThrowsException() throws Exception {
            // Given
            when(detectionService.ingestProducts(any(ProductIngestionRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            performIngestionRequest(validIngestionRequest)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 400 for products with missing required fields")
        void shouldReturn400ForProductsWithMissingFields() throws Exception {
            // Given
            var invalidRequest = new ProductIngestionRequest(
                List.of(
                    Map.of(
                        "description", "Missing name field",
                        "category", "Electronics"
                    )
                )
            );

            // When & Then
            performIngestionRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Missing required field: name")));
        }

        private ResultActions performIngestionRequest(ProductIngestionRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @DisplayName("Common API Tests")
    class CommonApiTests {

        @Test
        @DisplayName("Should return 415 for unsupported media type")
        void shouldReturn415ForUnsupportedMediaType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/detect")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));
        }

        @Test
        @DisplayName("Should return 405 for unsupported HTTP method")
        void shouldReturn405ForUnsupportedHttpMethod() throws Exception {
            // When & Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/detect"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
        }

        @Test
        @DisplayName("Should include correlation ID in response headers")
        void shouldIncludeCorrelationIdInResponseHeaders() throws Exception {
            // Given
            when(detectionService.detectProductType(any(ProductDetectionRequest.class)))
                .thenReturn(mockDetectionResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/detect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validDetectionRequest))
                    .header("X-Correlation-ID", "test-correlation-123"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(header().string("X-Correlation-ID", "test-correlation-123"));
        }
    }
}