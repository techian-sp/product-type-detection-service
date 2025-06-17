package com.lilyai.producttypedetection.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public final class ValidationException extends RuntimeException {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationException.class);
    
    private final String errorId;
    private final Instant timestamp;
    private final List<ValidationError> validationErrors;
    private final Map<String, Object> context;
    
    public ValidationException(String message) {
        this(message, List.of(), Map.of());
    }
    
    public ValidationException(String message, List<ValidationError> validationErrors) {
        this(message, validationErrors, Map.of());
    }
    
    public ValidationException(String message, List<ValidationError> validationErrors, Map<String, Object> context) {
        super(message);
        this.errorId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.validationErrors = List.copyOf(validationErrors);
        this.context = Map.copyOf(context);
        logValidationError();
    }
    
    public ValidationException(String message, Throwable cause) {
        this(message, cause, List.of(), Map.of());
    }
    
    public ValidationException(String message, Throwable cause, List<ValidationError> validationErrors, Map<String, Object> context) {
        super(message, cause);
        this.errorId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.validationErrors = List.copyOf(validationErrors);
        this.context = Map.copyOf(context);
        logValidationError();
    }
    
    private void logValidationError() {
        var logMessage = STR."Validation error occurred - ID: \{errorId}, Message: \{getMessage()}, Errors: \{validationErrors.size()}, Context: \{context}";
        
        if (getCause() != null) {
            logger.error(logMessage, getCause());
        } else {
            logger.warn(logMessage);
        }
        
        validationErrors.forEach(error -> 
            logger.debug(STR."Validation error detail - Field: \{error.field()}, Code: \{error.code()}, Message: \{error.message()}")
        );
    }
    
    public String getErrorId() {
        return errorId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
    
    public List<ValidationError> getErrorsForField(String field) {
        return validationErrors.stream()
            .filter(error -> field.equals(error.field()))
            .toList();
    }
    
    public static ValidationException of(String message, String field, String code, String fieldMessage) {
        return new ValidationException(
            message,
            List.of(new ValidationError(field, code, fieldMessage))
        );
    }
    
    public static ValidationException withContext(String message, Map<String, Object> context) {
        return new ValidationException(message, List.of(), context);
    }
    
    public record ValidationError(
        String field,
        String code,
        String message
    ) {
        public ValidationError {
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("Field cannot be null or blank");
            }
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Code cannot be null or blank");
            }
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Message cannot be null or blank");
            }
        }
    }
}