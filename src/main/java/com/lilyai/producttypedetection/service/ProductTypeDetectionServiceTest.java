package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.dto.ProductDetectionRequest;
import com.lilyai.producttypedetection.dto.ProductDetectionResponse;
import com.lilyai.producttypedetection.exception.InvalidProductDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class ProductTypeDetectionServiceTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ProductTypeDetectionService productTypeDetectionService;

    private ProductDetectionRequest validRequest;
    private ProductDetectionRequest nullFieldsRequest;
    private ProductDetectionRequest emptyFieldsRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ProductDetectionRequest(
            "iPhone 15 Pro Max",
            "Latest Apple smartphone with advanced camera system",
            "Apple",
            "Electronics",
            1299.99
        );

        nullFieldsRequest = new ProductDetectionRequest(
            null,
            null,
            null,
            null,
            null
        );

        emptyFieldsRequest = new ProductDetectionRequest(
            "",
            "",
            "",
            "",
            0.0
        );
    }

    @Test
    void detectProductType_WithValidRequest_ShouldReturnSuccessResponse(CapturedOutput output) {
        // When
        ProductDetectionResponse response = productTypeDetectionService.detectProductType(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("ELECTRONICS", response.detectedType());
        assertEquals(0.95, response.confidence());
        assertTrue(response.success());
        assertNull(response.errorMessage());
        
        // Verify logger output
        assertTrue(output.getOut().contains("Processing product type detection for: iPhone 15 Pro Max"));
        assertTrue(output.getOut().contains("Successfully detected product type: ELECTRONICS with confidence: 0.95"));
    }

    @Test
    void detectProductType_WithNullProductName_ShouldThrowException() {
        // When & Then
        InvalidProductDataException exception = assertThrows(
            InvalidProductDataException.class,
            () -> productTypeDetectionService.detectProductType(nullFieldsRequest)
        );
        
        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    void detectProductType_WithEmptyProductName_ShouldThrowException() {
        // When & Then
        InvalidProductDataException exception = assertThrows(
            InvalidProductDataException.class,
            () -> productTypeDetectionService.detectProductType(emptyFieldsRequest)
        );
        
        assertEquals("Product name cannot be null or empty", exception.getMessage());
    }

    @Test
    void detectProductType_WithNullDescription_ShouldThrowException() {
        var requestWithNullDescription = new ProductDetectionRequest(
            "Valid Product",
            null,
            "Valid Brand",
            "Valid Category",
            100.0
        );

        // When & Then
        InvalidProductDataException exception = assertThrows(
            InvalidProductDataException.class,
            () -> productTypeDetectionService.detectProductType(requestWithNullDescription)
        );
        
        assertEquals("Product description cannot be null or empty", exception.getMessage());
    }

    @Test
    void detectProductType_WithEmptyDescription_ShouldThrowException() {
        var requestWithEmptyDescription = new ProductDetectionRequest(
            "Valid Product",
            "",
            "Valid Brand",
            "Valid Category",
            100.0
        );

        // When & Then
        InvalidProductDataException exception = assertThrows(
            InvalidProductDataException.class,
            () -> productTypeDetectionService.detectProductType(requestWithEmptyDescription)
        );
        
        assertEquals("Product description cannot be null or empty", exception.getMessage());
    }

    @Test
    void detectProductType_WithNullPrice_ShouldThrowException() {
        var requestWithNullPrice = new ProductDetectionRequest(
            "Valid Product",
            "Valid Description",
            "Valid Brand",
            "Valid Category",
            null
        );

        // When & Then
        InvalidProductDataException exception = assertThrows(
            InvalidProductDataException.class,
            () -> productTypeDetectionService.detectProductType(requestWithNullPrice)
        );
        
        assertEquals("Product price cannot be null or negative", exception.getMessage());
    }

    @Test
    void detectProductType_WithNegativePrice_ShouldThrowException() {
        var requestWithNegativePrice = new ProductDetectionRequest(
            "Valid Product",
            "Valid Description",
            "Valid Brand",
            "Valid Category",
            -10.0
        );

        // When & Then
        InvalidProductDataException exception = assertThrows(
            InvalidProductDataException.class,
            () -> productTypeDetectionService.detectProductType(requestWithNegativePrice)
        );
        
        assertEquals("Product price cannot be null or negative", exception.getMessage());
    }

    @Test
    void detectProductType_WithClothingKeywords_ShouldReturnClothingType(CapturedOutput output) {
        var clothingRequest = new ProductDetectionRequest(
            "Cotton T-Shirt",
            "Comfortable cotton t-shirt for casual wear",
            "Nike",
            "Apparel",
            29.99
        );

        // When
        ProductDetectionResponse response = productTypeDetectionService.detectProductType(clothingRequest);

        // Then
        assertNotNull(response);
        assertEquals("CLOTHING", response.detectedType());
        assertEquals(0.88, response.confidence());
        assertTrue(response.success());
        
        // Verify logger output
        assertTrue(output.getOut().contains("Processing product type detection for: Cotton T-Shirt"));
        assertTrue(output.getOut().contains("Successfully detected product type: CLOTHING with confidence: 0.88"));
    }

    @Test
    void detectProductType_WithBookKeywords_ShouldReturnBooksType(CapturedOutput output) {
        var bookRequest = new ProductDetectionRequest(
            "Java Programming Guide",
            "Comprehensive guide to Java programming language",
            "O'Reilly",
            "Books",
            49.99
        );

        // When
        ProductDetectionResponse response = productTypeDetectionService.detectProductType(bookRequest);

        // Then
        assertNotNull(response);
        assertEquals("BOOKS", response.detectedType());
        assertEquals(0.92, response.confidence());
        assertTrue(response.success());
        
        // Verify logger output
        assertTrue(output.getOut().contains("Processing product type detection for: Java Programming Guide"));
        assertTrue(output.getOut().contains("Successfully detected product type: BOOKS with confidence: 0.92"));
    }

    @Test
    void detectProductType_WithUnknownProduct_ShouldReturnOtherType(CapturedOutput output) {
        var unknownRequest = new ProductDetectionRequest(
            "Mystery Item",
            "Unknown product description",
            "Unknown Brand",
            "Miscellaneous",
            19.99
        );

        // When
        ProductDetectionResponse response = productTypeDetectionService.detectProductType(unknownRequest);

        // Then
        assertNotNull(response);
        assertEquals("OTHER", response.detectedType());
        assertEquals(0.60, response.confidence());
        assertTrue(response.success());
        
        // Verify logger output
        assertTrue(output.getOut().contains("Processing product type detection for: Mystery Item"));
        assertTrue(output.getOut().contains("Successfully detected product type: OTHER with confidence: 0.60"));
    }
}