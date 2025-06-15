```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class BatchIngestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchIngestApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
@Validated
class BatchIngestController {

    @PostMapping("/batch-ingest")
    public ResponseEntity<BatchIngestionResult> batchIngest(@RequestBody List<ProductRequest> productRequests) {
        var successfulCount = 0;
        var errorMessages = new StringBuilder();

        for (var productRequest : productRequests) {
            try {
                processProductRequest(productRequest);
                successfulCount++;
            } catch (Exception e) {
                errorMessages.append(e.getMessage()).append("\n");
            }
        }

        var result = new BatchIngestionResult(successfulCount, productRequests.size() - successfulCount, errorMessages.toString());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private void processProductRequest(ProductRequest productRequest) throws Exception {
        // Simulate processing logic
        if (productRequest.name() == null || productRequest.name().isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        // Additional processing logic here
    }
}

record ProductRequest(String name, double price, String description) {}

record BatchIngestionResult(int successfulCount, int failedCount, String errorMessages) {}
```