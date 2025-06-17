package com.lilyai.producttypedetection.validator;

import com.lilyai.producttypedetection.dto.ProductRequest;
import com.lilyai.producttypedetection.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductRequestValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductRequestValidator.class);
    
    public void validate(ProductRequest request, String filename) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            String errorMsg = "ProductRequest cannot be null";
            logger.warn("Validation failed for file: {} - {}", filename, errorMsg);
            throw new ValidationException(errorMsg);
        }
        
        validateProductName(request.productName(), errors);
        validateProductDescription(request.productDescription(), errors);
        validateProductCategory(request.productCategory(), errors);
        validateProductPrice(request.productPrice(), errors);
        validateProductBrand(request.productBrand(), errors);
        
        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            logger.warn("Validation failed for file: {} - Fields: {} - Errors: {}", 
                       filename, 
                       getFieldNames(request), 
                       errorMessage);
            throw new ValidationException("Validation failed: " + errorMessage);
        }
    }
    
    private void validateProductName(String productName, List<String> errors) {
        if (!StringUtils.hasText(productName)) {
            errors.add("Product name is required");
        } else if (productName.trim().length() < 2) {
            errors.add("Product name must be at least 2 characters long");
        } else if (productName.trim().length() > 255) {
            errors.add("Product name must not exceed 255 characters");
        }
    }
    
    private void validateProductDescription(String productDescription, List<String> errors) {
        if (StringUtils.hasText(productDescription) && productDescription.trim().length() > 1000) {
            errors.add("Product description must not exceed 1000 characters");
        }
    }
    
    private void validateProductCategory(String productCategory, List<String> errors) {
        if (!StringUtils.hasText(productCategory)) {
            errors.add("Product category is required");
        } else if (productCategory.trim().length() < 2) {
            errors.add("Product category must be at least 2 characters long");
        } else if (productCategory.trim().length() > 100) {
            errors.add("Product category must not exceed 100 characters");
        }
    }
    
    private void validateProductPrice(Double productPrice, List<String> errors) {
        if (productPrice == null) {
            errors.add("Product price is required");
        } else if (productPrice < 0) {
            errors.add("Product price must be non-negative");
        } else if (productPrice > 999999.99) {
            errors.add("Product price must not exceed 999999.99");
        }
    }
    
    private void validateProductBrand(String productBrand, List<String> errors) {
        if (StringUtils.hasText(productBrand)) {
            if (productBrand.trim().length() < 2) {
                errors.add("Product brand must be at least 2 characters long when provided");
            } else if (productBrand.trim().length() > 100) {
                errors.add("Product brand must not exceed 100 characters");
            }
        }
    }
    
    private String getFieldNames(ProductRequest request) {
        return STR."productName=\{request.productName()}, productDescription=\{request.productDescription()}, productCategory=\{request.productCategory()}, productPrice=\{request.productPrice()}, productBrand=\{request.productBrand()}";
    }
}