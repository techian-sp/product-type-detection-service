```java
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelProcessor {

    private static final Logger logger = Logger.getLogger(ParallelProcessor.class.getName());
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        List<Integer> input = IntStream.rangeClosed(1, 100).boxed().toList();
        int chunkSize = 10;
        processInChunks(input, chunkSize);
        executorService.shutdown();
    }

    private static void processInChunks(List<Integer> input, int chunkSize) {
        List<List<Integer>> chunks = IntStream.range(0, (input.size() + chunkSize - 1) / chunkSize)
                .mapToObj(i -> input.subList(i * chunkSize, Math.min(input.size(), (i + 1) * chunkSize)))
                .toList();

        List<CompletableFuture<Void>> futures = chunks.stream()
                .map(chunk -> CompletableFuture.runAsync(() -> processChunk(chunk), executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

    private static void processChunk(List<Integer> chunk) {
        long startTime = System.nanoTime();
        // Simulate processing
        chunk.forEach(item -> {
            // Processing logic here
        });
        long duration = System.nanoTime() - startTime;
        logger.info(() -> String.format("Processed chunk %s in %d ns", chunk, duration));
    }
}
```