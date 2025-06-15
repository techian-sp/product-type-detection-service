```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableWebMvc
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class MyController {
    private static final Logger logger = LoggerFactory.getLogger(MyController.class);

    @GetMapping("/example")
    public String exampleEndpoint() {
        throw new ValidationException("Invalid input provided");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptions(Exception ex, WebRequest request) {
        return switch (ex) {
            case ValidationException ve -> {
                logger.warn("Validation error: {}", ve.getMessage());
                yield new ResponseEntity<>(new ErrorResponse("Validation Error", ve.getMessage()), HttpStatus.BAD_REQUEST);
            }
            default -> {
                logger.error("Internal server error: {}", ex.getMessage(), ex);
                yield new ResponseEntity<>(new ErrorResponse("Internal Server Error", "An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
}

record ErrorResponse(String error, String message) {}

class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```