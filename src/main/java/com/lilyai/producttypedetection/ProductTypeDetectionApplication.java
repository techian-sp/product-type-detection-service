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
 * best practices including:
 * </p>
 * <ul>
 *   <li>Configuration properties scanning</li>
 *   <li>Comprehensive error handling</li>
 *   <li>CORS configuration for cross-origin requests</li>
 *   <li>Structured logging</li>
 * </ul>
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
     * Main entry point for the Spring Boot application.
     * <p>
     * Initializes the Spring application context and starts the embedded server.
     * Includes proper exception handling and logging for application startup.
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
     * This configuration allows the API to be accessed from different origins,
     * which is essential for frontend applications running on different ports
     * or domains during development and production.
     * </p>
     * <p>
     * Security considerations:
     * </p>
     * <ul>
     *   <li>Allows common HTTP methods (GET, POST, PUT, DELETE, PATCH)</li>
     *   <li>Permits standard headers including Authorization</li>
     *   <li>Enables credentials for authenticated requests</li>
     *   <li>Sets appropriate cache duration for preflight requests</li>
     * </ul>
     *
     * @return WebMvcConfigurer instance with CORS configuration
     * @see WebMvcConfigurer
     * @see CorsRegistry
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
                
                logger.debug("CORS configuration applied for /api/** endpoints");
            }
        };
    }
}