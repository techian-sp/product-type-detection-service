```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ProductTypeDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductTypeDetectionApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
class ProductTypeDetectionController {

    private static final Logger logger = LoggerFactory.getLogger(ProductTypeDetectionController.class);

    @PostMapping("/product-type-detection")
    public ResponseEntity<ProductTypeResponse> detectProductType(@RequestBody ProductRequest request) {
        logger.info("Received request: {}", request);

        ProductTypeResponse response = switch (request) {
            case ProductRequest(String name, String category) when category.equalsIgnoreCase("Electronics") -> 
                new ProductTypeResponse(name, "Gadget");
            case ProductRequest(String name, String category) when category.equalsIgnoreCase("Clothing") -> 
                new ProductTypeResponse(name, "Apparel");
            default -> 
                new ProductTypeResponse(request.name(), "Unknown");
        };

        logger.info("Returning response: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

record ProductRequest(String name, String category) {}

record ProductTypeResponse(String name, String type) {}
```