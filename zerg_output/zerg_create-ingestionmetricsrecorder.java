```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SpringBootApplication
public class BatchProcessorApplication {

    private static final Logger logger = LoggerFactory.getLogger(BatchProcessorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BatchProcessorApplication.class, args);
        BatchProcessor processor = new BatchProcessor();
        processor.processBatch(List.of("data1", "data2", "data3", "duplicate", "data4", "failure"));
    }
}

class BatchProcessor {

    private int receivedCount = 0;
    private int ingestedCount = 0;
    private int duplicateCount = 0;
    private int failureCount = 0;

    public void processBatch(List<String> dataBatch) {
        dataBatch.forEach(this::processData);
        logBatchSummary();
    }

    private void processData(String data) {
        receivedCount++;
        switch (data) {
            case "duplicate" -> duplicateCount++;
            case "failure" -> failureCount++;
            default -> ingestedCount++;
        }
    }

    private void logBatchSummary() {
        String summary = """
                Batch Summary:
                Received: %d
                Ingested: %d
                Duplicates: %d
                Failures: %d
                """.formatted(receivedCount, ingestedCount, duplicateCount, failureCount);
        logger.info(summary);
    }
}
```