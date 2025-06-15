```java
import java.util.List;

public record BatchResultDTO(int successCount, int duplicateCount, int failedCount, List<String> errorMessages) {

    public BatchResultDTO {
        if (successCount < 0 || duplicateCount < 0 || failedCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }
        errorMessages = List.copyOf(errorMessages);
    }

    public String summary() {
        return String.format("""
            Batch Result Summary:
            Success Count: %d
            Duplicate Count: %d
            Failed Count: %d
            Error Messages: %s
            """, successCount, duplicateCount, failedCount, errorMessages);
    }

    public static void main(String[] args) {
        BatchResultDTO result = new BatchResultDTO(10, 2, 1, List.of("Error 1", "Error 2"));
        System.out.println(result.summary());
    }
}
```