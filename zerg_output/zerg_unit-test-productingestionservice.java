```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
public class BatchProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingApplication.class, args);
    }

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}

@Service
@Validated
class BatchService {

    private static final Logger logger = Logger.getLogger(BatchService.class.getName());
    private final ConcurrentLinkedQueue<Record> queue = new ConcurrentLinkedQueue<>();
    private final Validator validator;

    public BatchService(Validator validator) {
        this.validator = validator;
    }

    public void addRecord(@Valid Record record) {
        queue.add(record);
    }

    @Scheduled(fixedRate = 5000)
    public void processBatch() {
        List<Record> batch = queue.stream().distinct().collect(Collectors.toList());
        queue.clear();

        batch.forEach(record -> {
            Set<ConstraintViolation<Record>> violations = validator.validate(record);
            if (!violations.isEmpty()) {
                violations.forEach(violation -> logger.warning(violation.getMessage()));
                return;
            }
            updateMetrics(record);
            logger.info(String.format("Processed record: %s", record));
        });
    }

    private void updateMetrics(Record record) {
        // Simulate metrics update
        logger.info(String.format("Metrics updated for: %s", record));
    }
}

record Record(@NotNull String id, @NotNull String data) {}
```