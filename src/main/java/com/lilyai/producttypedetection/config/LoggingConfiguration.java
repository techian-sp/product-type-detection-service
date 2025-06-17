package com.lilyai.producttypedetection.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.classic.Level;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Configuration
public class LoggingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);
    
    private final Environment environment;
    
    public LoggingConfiguration(Environment environment) {
        this.environment = environment;
    }
    
    @PostConstruct
    public void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Configure console appender
        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE");
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n");
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        // Configure file appender
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("FILE");
        fileAppender.setFile("logs/product-type-detection.log");
        
        TimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern("logs/product-type-detection.%d{yyyy-MM-dd}.%i.log.gz");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.start();
        
        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg %X{userId:-} %X{requestId:-} %X{operation:-}%n");
        fileEncoder.start();
        
        fileAppender.setEncoder(fileEncoder);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();
        
        // Configure root logger
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);
        rootLogger.setLevel(Level.INFO);
        
        logger.info("Structured logging configuration completed for environment: {}", 
                   environment.getActiveProfiles());
    }
    
    @Bean
    public StructuredLogger structuredLogger() {
        return new StructuredLogger();
    }
    
    @Component
    public static class StructuredLogger {
        
        private static final Logger log = LoggerFactory.getLogger(StructuredLogger.class);
        
        public void info(String message, Map<String, Object> context) {
            setMDCContext(context);
            try {
                log.info(message);
            } finally {
                clearMDCContext(context);
            }
        }
        
        public void info(String message, Object... args) {
            log.info(message, args);
        }
        
        public void warn(String message, Map<String, Object> context) {
            setMDCContext(context);
            try {
                log.warn(message);
            } finally {
                clearMDCContext(context);
            }
        }
        
        public void warn(String message, Object... args) {
            log.warn(message, args);
        }
        
        public void error(String message, Throwable throwable, Map<String, Object> context) {
            setMDCContext(context);
            try {
                log.error(message, throwable);
            } finally {
                clearMDCContext(context);
            }
        }
        
        public void error(String message, Throwable throwable) {
            log.error(message, throwable);
        }
        
        public void error(String message, Object... args) {
            log.error(message, args);
        }
        
        private void setMDCContext(Map<String, Object> context) {
            if (context != null) {
                context.forEach((key, value) -> 
                    MDC.put(key, value != null ? value.toString() : "null"));
            }
            
            // Set default trace context if not present
            if (MDC.get("traceId") == null) {
                MDC.put("traceId", UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            }
            if (MDC.get("spanId") == null) {
                MDC.put("spanId", UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            }
            if (MDC.get("timestamp") == null) {
                MDC.put("timestamp", Instant.now().toString());
            }
        }
        
        private void clearMDCContext(Map<String, Object> context) {
            if (context != null) {
                context.keySet().forEach(MDC::remove);
            }
        }
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        logger.info("Product Type Detection Service started successfully with profiles: {}", 
                   String.join(",", environment.getActiveProfiles()));
    }
}