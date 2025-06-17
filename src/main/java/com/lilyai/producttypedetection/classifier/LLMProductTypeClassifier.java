package com.lilyai.producttypedetection.classifier;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConfigurationProperties(prefix = "llm.classifier")
public class LLMProductTypeClassifier {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMProductTypeClassifier.class);
    
    private final Duration simulatedLatency;
    private final List<String> dummyProductTypes;
    private final Map<String, Double> confidenceRanges;
    
    public LLMProductTypeClassifier(
            @DefaultValue("500ms") Duration simulatedLatency,
            @DefaultValue("Electronics,Clothing,Books,Home & Garden,Sports,Toys,Beauty,Automotive") List<String> dummyProductTypes,
            @DefaultValue("{\"min\": 0.7, \"max\": 0.95}") Map<String, Double> confidenceRanges) {
        this.simulatedLatency = simulatedLatency;
        this.dummyProductTypes = dummyProductTypes;
        this.confidenceRanges = confidenceRanges;
        logger.info("LLM Product Type Classifier initialized with {} dummy types", dummyProductTypes.size());
    }
    
    public CompletableFuture<ClassificationResult> classifyAsync(String productDescription) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Starting LLM classification for product: {}", 
                productDescription.substring(0, Math.min(50, productDescription.length())));
            
            // Simulate LLM processing time
            try {
                Thread.sleep(simulatedLatency.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Classification interrupted", e);
            }
            
            // Generate dummy classification result
            var randomType = dummyProductTypes.get(
                ThreadLocalRandom.current().nextInt(dummyProductTypes.size()));
            
            var confidence = ThreadLocalRandom.current().nextDouble(
                confidenceRanges.get("min"), 
                confidenceRanges.get("max"));
            
            var result = new ClassificationResult(randomType, confidence, "STUB_LLM_v1.0");
            
            logger.debug("LLM classification completed: {} with confidence {}", 
                result.productType(), result.confidence());
            
            return result;
        });
    }
    
    public ClassificationResult classify(String productDescription) {
        return classifyAsync(productDescription).join();
    }
    
    public record ClassificationResult(
            String productType,
            double confidence,
            String modelVersion
    ) {
        public ClassificationResult {
            if (productType == null || productType.isBlank()) {
                throw new IllegalArgumentException("Product type cannot be null or blank");
            }
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
            }
            if (modelVersion == null || modelVersion.isBlank()) {
                throw new IllegalArgumentException("Model version cannot be null or blank");
            }
        }
        
        public boolean isHighConfidence() {
            return confidence >= 0.8;
        }
    }
    
    // Configuration properties getters
    public Duration getSimulatedLatency() {
        return simulatedLatency;
    }
    
    public List<String> getDummyProductTypes() {
        return List.copyOf(dummyProductTypes);
    }
    
    public Map<String, Double> getConfidenceRanges() {
        return Map.copyOf(confidenceRanges);
    }
}