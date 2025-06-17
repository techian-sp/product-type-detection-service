package com.lilyai.producttypedetection.controller;

import com.lilyai.producttypedetection.dto.ProductDetectionRequest;
import com.lilyai.producttypedetection.dto.ProductDetectionResponse;
import com.lilyai.producttypedetection.dto.ErrorResponse;
import com.lilyai.producttypedetection.service.ProductTypeDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/product-detection")
@Tag(name = "Product Type Detection", description = "AI-powered product type detection and classification")
public class ProductTypeDetectionController {

    private final ProductTypeDetectionService detectionService;

    public ProductTypeDetectionController(ProductTypeDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping("/detect")
    @Operation(
        summary = "Detect product type from description",
        description = "Analyzes product description and metadata to determine the most likely product type using AI classification"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product type successfully detected",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDetectionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unable to process the product data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<ProductDetectionResponse> detectProductType(
        @RequestBody(
            description = "Product information for type detection",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDetectionRequest.class)
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody ProductDetectionRequest request
    ) {
        var response = detectionService.detectProductType(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detect/batch")
    @Operation(
        summary = "Batch detect product types",
        description = "Processes multiple products simultaneously for efficient bulk product type detection"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Batch processing completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDetectionResponse[].class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid batch request data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Batch size exceeds maximum limit",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public CompletableFuture<ResponseEntity<List<ProductDetectionResponse>>> detectProductTypesBatch(
        @RequestBody(
            description = "List of products for batch type detection",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDetectionRequest[].class)
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody List<ProductDetectionRequest> requests
    ) {
        return detectionService.detectProductTypesBatch(requests)
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/detect/image")
    @Operation(
        summary = "Detect product type from image",
        description = "Analyzes product image using computer vision to determine product type and characteristics"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image analysis completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDetectionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid image file or format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Image file size exceeds maximum limit",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<ProductDetectionResponse> detectProductTypeFromImage(
        @RequestParam("image") MultipartFile imageFile,
        @RequestParam(value = "description", required = false) String description
    ) {
        var response = detectionService.detectProductTypeFromImage(imageFile, description);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    @Operation(
        summary = "Get available product categories",
        description = "Retrieves the list of all supported product categories and their hierarchical structure"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "array", implementation = String.class)
            )
        )
    })
    public ResponseEntity<List<String>> getAvailableCategories() {
        var categories = detectionService.getAvailableCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check endpoint",
        description = "Checks the health status of the product detection service and its dependencies"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object")
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Service is unhealthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Object> healthCheck() {
        var healthStatus = detectionService.getHealthStatus();
        var status = healthStatus.isHealthy() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(healthStatus);
    }
}