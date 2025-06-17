package com.lilyai.producttypedetection.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    public static final String PRODUCT_TYPE_CACHE = "productTypeCache";
    public static final String CLASSIFICATION_CACHE = "classificationCache";
    public static final String MODEL_CACHE = "modelCache";
    
    private final ConcurrentMapCacheManager cacheManager;
    
    public CacheConfig() {
        this.cacheManager = new ConcurrentMapCacheManager();
    }
    
    @Bean
    public CacheManager cacheManager() {
        cacheManager.setCacheNames(
            PRODUCT_TYPE_CACHE,
            CLASSIFICATION_CACHE,
            MODEL_CACHE
        );
        cacheManager.setAllowNullValues(false);
        
        logger.info("Initialized CacheManager with caches: {}", 
                   String.join(", ", cacheManager.getCacheNames()));
        
        return cacheManager;
    }
    
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void logCacheStatistics() {
        var cacheNames = cacheManager.getCacheNames();
        
        for (String cacheName : cacheNames) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                var nativeCache = cache.getNativeCache();
                if (nativeCache instanceof java.util.concurrent.ConcurrentMap<?, ?> concurrentMap) {
                    int size = concurrentMap.size();
                    logger.info("Cache '{}' statistics - Size: {} entries", cacheName, size);
                    
                    if (logger.isDebugEnabled()) {
                        var memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        logger.debug("Cache '{}' - Current JVM memory usage: {} MB", 
                                   cacheName, memoryUsage / (1024 * 1024));
                    }
                }
            }
        }
    }
    
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("Evicted all entries from cache: {}", cacheName);
            }
        });
    }
    
    public void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            logger.info("Evicted all entries from cache: {}", cacheName);
        } else {
            logger.warn("Cache '{}' not found for eviction", cacheName);
        }
    }
}