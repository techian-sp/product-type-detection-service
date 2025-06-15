package com.example.project.model;

import java.util.List;

public class ProductTypeRequest {
    private String title;
    private String description;
    private List<String> imageUrls;

    public ProductTypeRequest() {
    }

    public ProductTypeRequest(String title, String description, List<String> imageUrls) {
        this.title = title;
        this.description = description;
        this.imageUrls = imageUrls;
    }

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