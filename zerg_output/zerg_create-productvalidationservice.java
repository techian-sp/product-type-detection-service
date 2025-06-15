```java
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.logging.Logger;

@Service
public class ProductRequestValidator {

    private static final Logger logger = Logger.getLogger(ProductRequestValidator.class.getName());

    public void validate(ProductRequest productRequest) throws ValidationException {
        var errors = new StringBuilder();

        if (productRequest.name() == null || productRequest.name().isBlank()) {
            errors.append("Name is required. ");
        }
        if (productRequest.price() <= 0) {
            errors.append("Price must be greater than zero. ");
        }
        if (productRequest.file() == null || productRequest.file().isEmpty()) {
            errors.append("File is required. ");
        }

        if (errors.length() > 0) {
            var errorMessage = String.format("Validation failed for file: %s. Errors: %s",
                    productRequest.file() != null ? productRequest.file().getOriginalFilename() : "No file",
                    errors.toString());
            logger.warning(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    public record ProductRequest(String name, double price, MultipartFile file) {}

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}
```