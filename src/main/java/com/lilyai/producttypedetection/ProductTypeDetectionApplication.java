package com.lilyai.producttypedetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableTransactionManagement
@EnableJpaRepositories
@ConfigurationPropertiesScan
@EnableWebSecurity
public class ProductTypeDetectionApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductTypeDetectionApplication.class);
    
    public static void main(String[] args) {
        var context = SpringApplication.run(ProductTypeDetectionApplication.class, args);
        
        // Java 23 features demonstration
        var applicationInfo = STR."Application started with context: \{context.getDisplayName()}";
        logger.info(applicationInfo);
        
        // Pattern matching with switch expressions
        var startupMessage = switch (args.length) {
            case 0 -> "Started with default configuration";
            case 1 -> STR."Started with profile: \{args[0]}";
            default -> STR."Started with \{args.length} arguments";
        };
        
        logger.info(startupMessage);
        
        // Record patterns and sealed classes usage
        processStartupData(new StartupData("ProductTypeDetection", "1.0.0", List.of("detection", "classification")));
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ProductDetection-");
        executor.initialize();
        return executor;
    }
    
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
    
    // Java 23 Record with validation
    public record StartupData(String appName, String version, List<String> features) {
        public StartupData {
            if (appName == null || appName.isBlank()) {
                throw new IllegalArgumentException("Application name cannot be null or blank");
            }
            if (version == null || version.isBlank()) {
                throw new IllegalArgumentException("Version cannot be null or blank");
            }
            features = List.copyOf(features != null ? features : List.of());
        }
    }
    
    // Java 23 Sealed interface for application states
    public sealed interface ApplicationState permits Running, Starting, Stopping {
        String getDescription();
    }
    
    public record Running(long startTime) implements ApplicationState {
        @Override
        public String getDescription() {
            return STR."Application running since \{startTime}";
        }
    }
    
    public record Starting(String phase) implements ApplicationState {
        @Override
        public String getDescription() {
            return STR."Application starting: \{phase}";
        }
    }
    
    public record Stopping(String reason) implements ApplicationState {
        @Override
        public String getDescription() {
            return STR."Application stopping: \{reason}";
        }
    }
    
    // Method using pattern matching and string templates
    private static void processStartupData(StartupData data) {
        var message = switch (data) {
            case StartupData(var name, var version, var features) when features.isEmpty() -> 
                STR."\{name} v\{version} started with no additional features";
            case StartupData(var name, var version, var features) when features.size() == 1 -> 
                STR."\{name} v\{version} started with feature: \{features.get(0)}";
            case StartupData(var name, var version, var features) -> 
                STR."\{name} v\{version} started with \{features.size()} features: \{String.join(", ", features)}";
        };
        
        logger.info(message);
        
        // Async processing with CompletableFuture and virtual threads
        CompletableFuture.runAsync(() -> {
            Thread.ofVirtual().name("startup-processor").start(() -> {
                try {
                    Thread.sleep(100); // Simulate processing
                    logger.debug("Startup data processed asynchronously");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Startup processing interrupted");
                }
            });
        });
    }
    
    // Method demonstrating pattern matching with collections
    public String analyzeFeatures(List<String> features) {
        return switch (features) {
            case List<String> list when list.isEmpty() -> "No features configured";
            case List<String> list when list.contains("detection") && list.contains("classification") -> 
                "Full AI capabilities enabled";
            case List<String> list when list.contains("detection") -> "Detection only mode";
            case List<String> list when list.contains("classification") -> "Classification only mode";
            default -> STR."Custom configuration with \{features.size()} features";
        };
    }
}