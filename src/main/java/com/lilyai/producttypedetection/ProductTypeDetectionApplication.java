package com.lilyai.producttypedetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Spring Boot application class for the Product Type Detection Service.
 * <p>
 * This application provides REST APIs for detecting and classifying product types
 * based on various input parameters and machine learning algorithms.
 * </p>
 * <p>
 * Built with Java 23.0 and Spring Boot 3.4.0, following modern Spring Boot
 * best practices including configuration properties scanning and proper
 * error handling mechanisms.
 * </p>
 *
 * @author LilyAI Development Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductTypeDetectionApplication {

    private static final Logger logger = LoggerFactory.getLogger(ProductTypeDetectionApplication.class);

    /**
     * Main entry point for the Product Type Detection Service application.
     * <p>
     * Initializes the Spring Boot application context and starts the embedded
     * web server. Includes proper error handling for application startup failures.
     * </p>
     *
     * @param args command line arguments passed to the application
     * @throws IllegalStateException if the application fails to start properly
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Product Type Detection Service...");
            var context = SpringApplication.run(ProductTypeDetectionApplication.class, args);
            logger.info("Product Type Detection Service started successfully on port: {}", 
                context.getEnvironment().getProperty("server.port", "8080"));
        } catch (Exception e) {
            logger.error("Failed to start Product Type Detection Service: {}", e.getMessage(), e);
            throw new IllegalStateException("Application startup failed", e);
        }
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings for the application.
     * <p>
     * Allows controlled access from web browsers and other clients by defining
     * which origins, methods, and headers are permitted for cross-origin requests.
     * </p>
     *
     * @return WebMvcConfigurer instance with CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Adds CORS mappings to allow cross-origin requests.
             *
             * @param registry the CORS registry to configure
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
                
                logger.debug("CORS configuration applied for /api/** endpoints");
            }
        };
    }
}