```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SpringBootApplication
public class BatchProcessingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingApplication.class, args);
    }
}

@RestController
class BatchController {
    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    @PostMapping("/processBatch")
    public BatchSummary processBatch(@RequestBody List<DataRecord> records) {
        logger.info("Starting batch processing.");

        var validRecords = records.stream()
                .filter(this::validate)
                .toList();
        logger.info("Validation complete. Valid records count: {}", validRecords.size());

        var ingestedRecords = validRecords.stream()
                .map(this::ingest)
                .toList();
        logger.info("Ingestion complete. Ingested records count: {}", ingestedRecords.size());

        var detections = ingestedRecords.stream()
                .map(this::detect)
                .toList();
        logger.info("Detection complete. Detections count: {}", detections.size());

        var summary = new BatchSummary(validRecords.size(), ingestedRecords.size(), detections.size());
        logger.info("Batch processing complete. Summary: {}", summary);

        return summary;
    }

    private boolean validate(DataRecord record) {
        return switch (record) {
            case DataRecord(String data) when !data.isBlank() -> true;
            default -> false;
        };
    }

    private IngestedRecord ingest(DataRecord record) {
        return new IngestedRecord(record.data().toUpperCase());
    }

    private Detection detect(IngestedRecord record) {
        return new Detection(record.data().contains("ALERT"));
    }
}

record DataRecord(String data) {}
record IngestedRecord(String data) {}
record Detection(boolean alert) {}
record BatchSummary(int validCount, int ingestedCount, int detectionCount) {}
```