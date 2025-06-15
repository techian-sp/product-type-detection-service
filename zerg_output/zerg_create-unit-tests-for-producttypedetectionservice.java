```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class DetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DetectionApplication.class, args);
    }

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager("detections");
    }
}

record DetectionResult(String source, boolean detected) {}

@Service
class DetectionService {

    private final ConcurrentHashMap<String, Boolean> detectionHistory = new ConcurrentHashMap<>();

    @Cacheable("detections")
    public DetectionResult detect(String source) {
        return switch (source) {
            case String s when detectionHistory.containsKey(s) -> new DetectionResult(s, false);
            default -> {
                detectionHistory.put(source, true);
                yield new DetectionResult(source, true);
            }
        };
    }
}

@RestController
class DetectionController {

    private final DetectionService detectionService;

    DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping("/detect")
    public List<DetectionResult> detectSources(List<String> sources) {
        return sources.stream()
                .map(detectionService::detect)
                .toList();
    }
}
```