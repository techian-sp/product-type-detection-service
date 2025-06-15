```java
// File: src/main/java/com/lilyai/producttypedetection/model/Product.java
package com.lilyai.producttypedetection.model;

public record Product(Long id, String name, String type) {}

// File: src/main/java/com/lilyai/producttypedetection/repository/ProductRepository.java
package com.lilyai.producttypedetection.repository;

import com.lilyai.producttypedetection.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {}

// File: src/main/java/com/lilyai/producttypedetection/service/ProductService.java
package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.model.Product;
import com.lilyai.producttypedetection.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

// File: src/main/java/com/lilyai/producttypedetection/controller/ProductController.java
package com.lilyai.producttypedetection.controller;

import com.lilyai.producttypedetection.model.Product;
import com.lilyai.producttypedetection.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.of(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

// File: src/main/java/com/lilyai/producttypedetection/config/AppConfig.java
package com.lilyai.producttypedetection.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    // Configuration beans can be added here
}

// File: src/main/java/com/lilyai/producttypedetection/util/StringUtil.java
package com.lilyai.producttypedetection.util;

public class StringUtil {
    public static String formatProductName(String name) {
        return name.strip().toUpperCase();
    }
}
```