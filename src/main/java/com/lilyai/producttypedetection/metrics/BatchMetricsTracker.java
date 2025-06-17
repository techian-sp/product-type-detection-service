package com.lilyai.producttypedetection.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;
import java.time.Duration;

@Component
public final class BatchMetricsTracker {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchMetricsTracker.class);
    
    private final AtomicLong receivedCount = new AtomicLong(0);
    private final AtomicLong ingestedCount = new AtomicLong(0);
    private final AtomicLong duplicatesCount = new AtomicLong(0);
    private final AtomicLong failuresCount = new AtomicLong(0);
    private volatile Instant batchStartTime;
    
    public void startBatch() {
        this.batchStartTime = Instant.now();
        resetCounters();
    }
    
    public void incrementReceived() {
        receivedCount.incrementAndGet();
    }
    
    public void incrementReceived(long count) {
        receivedCount.addAndGet(count);
    }
    
    public void incrementIngested() {
        ingestedCount.incrementAndGet();
    }
    
    public void incrementIngested(long count) {
        ingestedCount.addAndGet(count);
    }
    
    public void incrementDuplicates() {
        duplicatesCount.incrementAndGet();
    }
    
    public void incrementDuplicates(long count) {
        duplicatesCount.addAndGet(count);
    }
    
    public void incrementFailures() {
        failuresCount.incrementAndGet();
    }
    
    public void incrementFailures(long count) {
        failuresCount.addAndGet(count);
    }
    
    public void logBatchSummary() {
        var endTime = Instant.now();
        var duration = batchStartTime != null ? Duration.between(batchStartTime, endTime) : Duration.ZERO;
        
        logger.info("Batch processing summary - Received: {}, Ingested: {}, Duplicates: {}, Failures: {}, Duration: {}ms",
                receivedCount.get(),
                ingestedCount.get(),
                duplicatesCount.get(),
                failuresCount.get(),
                duration.toMillis());
    }
    
    public BatchSummary getCurrentSummary() {
        var endTime = Instant.now();
        var duration = batchStartTime != null ? Duration.between(batchStartTime, endTime) : Duration.ZERO;
        
        return new BatchSummary(
                receivedCount.get(),
                ingestedCount.get(),
                duplicatesCount.get(),
                failuresCount.get(),
                duration
        );
    }
    
    private void resetCounters() {
        receivedCount.set(0);
        ingestedCount.set(0);
        duplicatesCount.set(0);
        failuresCount.set(0);
    }
    
    public record BatchSummary(
            long received,
            long ingested,
            long duplicates,
            long failures,
            Duration processingDuration
    ) {
        public double getSuccessRate() {
            return received > 0 ? (double) ingested / received * 100.0 : 0.0;
        }
        
        public double getFailureRate() {
            return received > 0 ? (double) failures / received * 100.0 : 0.0;
        }
        
        public double getDuplicateRate() {
            return received > 0 ? (double) duplicates / received * 100.0 : 0.0;
        }
    }
}