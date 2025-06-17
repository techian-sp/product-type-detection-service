package com.lilyai.producttypedetection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

@Service
public final class ParallelChunkProcessor<T, R> {
    
    private static final Logger logger = LoggerFactory.getLogger(ParallelChunkProcessor.class);
    private static final int DEFAULT_CHUNK_SIZE = 100;
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    
    private final ExecutorService executorService;
    
    public ParallelChunkProcessor() {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    public ParallelChunkProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    public CompletableFuture<List<R>> processInParallel(
            List<T> input,
            Function<List<T>, List<R>> processor) {
        return processInParallel(input, processor, DEFAULT_CHUNK_SIZE);
    }
    
    public CompletableFuture<List<R>> processInParallel(
            List<T> input,
            Function<List<T>, List<R>> processor,
            int chunkSize) {
        
        if (input == null || input.isEmpty()) {
            logger.warn("Input list is null or empty");
            return CompletableFuture.completedFuture(List.of());
        }
        
        logger.info("Starting parallel processing of {} items with chunk size {}", 
                   input.size(), chunkSize);
        
        var chunks = createChunks(input, chunkSize);
        logger.info("Created {} chunks for processing", chunks.size());
        
        var chunkFutures = IntStream.range(0, chunks.size())
                .mapToObj(chunkIndex -> processChunk(chunks.get(chunkIndex), processor, chunkIndex))
                .toArray(CompletableFuture[]::new);
        
        return CompletableFuture.allOf(chunkFutures)
                .thenApply(_ -> {
                    var results = new ArrayList<R>();
                    for (var future : chunkFutures) {
                        try {
                            results.addAll(future.join());
                        } catch (Exception e) {
                            logger.error("Error processing chunk", e);
                            throw new RuntimeException("Failed to process chunk", e);
                        }
                    }
                    logger.info("Completed parallel processing. Total results: {}", results.size());
                    return List.copyOf(results);
                });
    }
    
    private CompletableFuture<List<R>> processChunk(
            List<T> chunk, 
            Function<List<T>, List<R>> processor, 
            int chunkIndex) {
        
        return CompletableFuture.supplyAsync(() -> {
            var startTime = Instant.now();
            logger.debug("Starting processing chunk {} with {} items", chunkIndex, chunk.size());
            
            try {
                var result = processor.apply(chunk);
                var duration = Duration.between(startTime, Instant.now());
                
                logger.info("Chunk {} processed successfully in {} ms. Input size: {}, Output size: {}",
                           chunkIndex, duration.toMillis(), chunk.size(), 
                           result != null ? result.size() : 0);
                
                return result != null ? result : List.<R>of();
                
            } catch (Exception e) {
                var duration = Duration.between(startTime, Instant.now());
                logger.error("Chunk {} failed after {} ms with error: {}", 
                            chunkIndex, duration.toMillis(), e.getMessage(), e);
                throw new RuntimeException(STR."Failed to process chunk \{chunkIndex}", e);
            }
        }, executorService);
    }
    
    private List<List<T>> createChunks(List<T> input, int chunkSize) {
        var chunks = new ArrayList<List<T>>();
        
        for (int i = 0; i < input.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, input.size());
            chunks.add(input.subList(i, endIndex));
        }
        
        return chunks;
    }
    
    public void shutdown() {
        logger.info("Shutting down ParallelChunkProcessor");
        executorService.shutdown();
    }
    
    public record ProcessingResult<R>(List<R> results, Duration totalDuration, int chunksProcessed) {}
    
    public CompletableFuture<ProcessingResult<R>> processWithMetrics(
            List<T> input,
            Function<List<T>, List<R>> processor,
            int chunkSize) {
        
        var overallStartTime = Instant.now();
        
        return processInParallel(input, processor, chunkSize)
                .thenApply(results -> {
                    var totalDuration = Duration.between(overallStartTime, Instant.now());
                    var chunksProcessed = (int) Math.ceil((double) input.size() / chunkSize);
                    
                    logger.info("Processing completed. Total duration: {} ms, Chunks: {}, Items: {}",
                               totalDuration.toMillis(), chunksProcessed, input.size());
                    
                    return new ProcessingResult<>(results, totalDuration, chunksProcessed);
                });
    }
}