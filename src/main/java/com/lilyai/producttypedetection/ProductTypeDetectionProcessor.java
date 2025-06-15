```java
package com.lilyai.producttypedetection;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProductTypeDetectionProcessor {

    private static final Logger logger = Logger.getLogger(ProductTypeDetectionProcessor.class.getName());
    private static final int CHUNK_SIZE = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        List<String> inputs = List.of("input1", "input2", "input3", "input4", "input5", "input6", "input7", "input8", "input9", "input10", "input11", "input12");
        new ProductTypeDetectionProcessor().processInputsInChunks(inputs);
        executorService.shutdown();
    }

    public void processInputsInChunks(List<String> inputs) {
        for (int i = 0; i < inputs.size(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, inputs.size());
            List<String> chunk = inputs.subList(i, end);
            CompletableFuture.runAsync(() -> processChunk(chunk), executorService)
                    .thenRun(() -> logChunkDuration(chunk));
        }
    }

    private void processChunk(List<String> chunk) {
        // Simulate processing
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void logChunkDuration(List<String> chunk) {
        String chunkInfo = String.format("Processed chunk: %s", chunk);
        logger.info(chunkInfo);
    }
}
```