package com.lilyai.producttypedetection.controller;

import com.lilyai.producttypedetection.dto.ProductRequest;
import com.lilyai.producttypedetection.dto.ProductTypeResponse;
import com.lilyai.producttypedetection.service.ProductTypeDetectionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductTypeDetectionController {

    private static final Logger logger = LoggerFactory.getLogger(ProductTypeDetectionController.class);
    
    private final ProductTypeDetectionService productTypeDetectionService;
    
    public ProductTypeDetectionController(ProductTypeDetectionService productTypeDetectionService) {
        this.productTypeDetectionService = productTypeDetectionService;
    }
    
    @PostMapping("/product-type-detection")
    public ResponseEntity<ProductTypeResponse> detectProductType(@Valid @RequestBody ProductRequest request) {
        logger.info("Received product type detection request: {}", request);
        
        try {
            ProductTypeResponse response = productTypeDetectionService.detectProductType(request);
            logger.info("Product type detection completed successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing product type detection request: {}", request, e);
            throw e;
        }
    }
}