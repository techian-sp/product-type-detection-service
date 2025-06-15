```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableCaching
public class CacheApplication {

    private static final Logger logger = LoggerFactory.getLogger(CacheApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("exampleCache") {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                var cache = super.createConcurrentMapCache(name);
                logCacheStats(name);
                return cache;
            }
        };
    }

    private void logCacheStats(String cacheName) {
        logger.info(String.format("Cache '%s' initialized with ConcurrentMapCacheManager", cacheName));
    }
}
```