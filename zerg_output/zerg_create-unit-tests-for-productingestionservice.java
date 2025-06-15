```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication
@EnableScheduling
public class BatchProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingApplication.class, args);
    }

    @Bean
    public Logger logger() {
        return Logger.getLogger(BatchProcessingApplication.class.getName());
    }
}

@RestController
class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping("/processBatch")
    public String processBatch() {
        return batchService.processBatch();
    }
}

@Service
class BatchService {

    private final BatchRepository batchRepository;
    private final Logger logger;

    public BatchService(BatchRepository batchRepository, Logger logger) {
        this.batchRepository = batchRepository;
        this.logger = logger;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scheduledBatchProcessing() {
        processBatch();
    }

    public String processBatch() {
        var items = batchRepository.fetchBatchItems();
        if (items.isEmpty()) {
            return "No items to process.";
        }

        var processedCount = 0;
        for (var item : items) {
            if (processItem(item)) {
                processedCount++;
            }
        }

        var resultMessage = switch (processedCount) {
            case 0 -> "No items processed.";
            case 1 -> "1 item processed.";
            default -> String.format("%d items processed.", processedCount);
        };

        logger.info(resultMessage);
        return resultMessage;
    }

    private boolean processItem(BatchItem item) {
        // Simulate item processing
        return true;
    }
}

interface BatchRepository {
    List<BatchItem> fetchBatchItems();
}

record BatchItem(int id, String data) {}
```