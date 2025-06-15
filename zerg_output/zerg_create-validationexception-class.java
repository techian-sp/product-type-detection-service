```java
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.logging.Logger;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    private static final Logger logger = Logger.getLogger(ValidationException.class.getName());
    private final String fieldName;
    private final String errorMessage;

    public ValidationException(String fieldName, String errorMessage) {
        super(String.format("Validation failed for field '%s': %s", fieldName, errorMessage));
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
        logError();
    }

    public String fieldName() {
        return fieldName;
    }

    public String errorMessage() {
        return errorMessage;
    }

    private void logError() {
        logger.severe(String.format("ValidationException: Field '%s' - %s", fieldName, errorMessage));
    }
}
```