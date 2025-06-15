```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/api")
class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    record RequestData(String field1, String field2) {}

    @PostMapping("/process")
    public ResponseEntity<String> processRequest(@RequestBody RequestData requestData) {
        try {
            validateRequestData(requestData);
            logger.info("Processing request with field1: {}, field2: {}", requestData.field1(), requestData.field2());
            return ResponseEntity.ok("Success");
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private void validateRequestData(RequestData requestData) {
        if (Objects.isNull(requestData) || isNullOrEmpty(requestData.field1()) || isNullOrEmpty(requestData.field2())) {
            throw new IllegalArgumentException("Fields must not be null or empty");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }
}
```