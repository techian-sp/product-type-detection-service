package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductTypeController {

    @PostMapping("/product-type")
    public ResponseEntity<String> createProductType(@RequestBody ProductTypeRequest request) {
        // Logic to handle the creation of a product type would go here

        return new ResponseEntity<>("Product type created successfully", HttpStatus.CREATED);
    }

    public static class ProductTypeRequest {
        private String title;
        private String description;
        private List<String> imageUrls;

        // Getters and setters

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }
    }
}