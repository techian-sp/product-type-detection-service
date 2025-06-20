package com.lilyai.producttypedetection.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * Liquibase configuration for Product Type Detection Service.
 * <p>
 * This configuration class sets up Liquibase for database schema management
 * with proper changelog organization for products and product images.
 * </p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Master changelog configuration with modular includes</li>
 *   <li>Conditional activation based on properties</li>
 *   <li>Production-ready error handling</li>
 *   <li>Comprehensive logging for database migrations</li>
 * </ul>
 *
 * @author Product Type Detection Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseConfig {

    private static final Logger logger = Logger.getLogger(LiquibaseConfig.class.getName());
    
    private static final String MASTER_CHANGELOG_PATH = "classpath:db/changelog/db.changelog-master.xml";
    private static final String LIQUIBASE_SCHEMA = "public";
    
    private final LiquibaseProperties liquibaseProperties;
    private final ResourceLoader resourceLoader;

    /**
     * Constructs a new LiquibaseConfig with the specified properties and resource loader.
     *
     * @param liquibaseProperties the Liquibase configuration properties
     * @param resourceLoader the Spring resource loader for accessing changelog files
     * @throws IllegalArgumentException if any required parameter is null
     */
    public LiquibaseConfig(LiquibaseProperties liquibaseProperties, ResourceLoader resourceLoader) {
        this.liquibaseProperties = validateNotNull(liquibaseProperties, "LiquibaseProperties cannot be null");
        this.resourceLoader = validateNotNull(resourceLoader, "ResourceLoader cannot be null");
        
        logger.info("Initializing Liquibase configuration for Product Type Detection Service");
    }

    /**
     * Creates and configures the SpringLiquibase bean for database migrations.
     * <p>
     * This bean manages the execution of database changelog files in the correct order:
     * <ol>
     *   <li>db.changelog-products.xml - Product entity schema</li>
     *   <li>db.changelog-product-images.xml - Product images schema</li>
     * </ol>
     * </p>
     *
     * @param dataSource the primary data source for the application
     * @return configured SpringLiquibase instance
     * @throws IllegalArgumentException if dataSource is null
     * @throws RuntimeException if changelog master file cannot be located
     */
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        validateNotNull(dataSource, "DataSource cannot be null");
        
        try {
            logger.info("Configuring SpringLiquibase with master changelog: " + MASTER_CHANGELOG_PATH);
            
            var liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog(MASTER_CHANGELOG_PATH);
            liquibase.setDefaultSchema(LIQUIBASE_SCHEMA);
            liquibase.setResourceLoader(resourceLoader);
            
            // Apply custom properties if configured
            configureCustomProperties(liquibase);
            
            // Validate changelog accessibility
            validateChangelogAccessibility();
            
            logger.info("SpringLiquibase configuration completed successfully");
            return liquibase;
            
        } catch (Exception e) {
            logger.severe("Failed to configure SpringLiquibase: " + e.getMessage());
            throw new RuntimeException("Liquibase configuration failed", e);
        }
    }

    /**
     * Applies custom Liquibase properties from application configuration.
     *
     * @param liquibase the SpringLiquibase instance to configure
     */
    private void configureCustomProperties(SpringLiquibase liquibase) {
        if (liquibaseProperties.getContexts() != null) {
            liquibase.setContexts(String.join(",", liquibaseProperties.getContexts()));
            logger.info("Applied Liquibase contexts: " + liquibase.getContexts());
        }
        
        if (liquibaseProperties.getLabels() != null) {
            liquibase.setLabels(String.join(",", liquibaseProperties.getLabels()));
            logger.info("Applied Liquibase labels: " + liquibase.getLabels());
        }
        
        if (liquibaseProperties.getDropFirst() != null) {
            liquibase.setDropFirst(liquibaseProperties.getDropFirst());
            logger.info("Drop first enabled: " + liquibase.isDropFirst());
        }
    }

    /**
     * Validates that the master changelog file is accessible.
     *
     * @throws RuntimeException if the changelog file cannot be accessed
     */
    private void validateChangelogAccessibility() {
        try {
            var resource = resourceLoader.getResource(MASTER_CHANGELOG_PATH);
            if (!resource.exists()) {
                throw new RuntimeException("Master changelog file not found: " + MASTER_CHANGELOG_PATH);
            }
            logger.info("Master changelog file validated: " + MASTER_CHANGELOG_PATH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate changelog accessibility", e);
        }
    }

    /**
     * Validates that the provided object is not null.
     *
     * @param <T> the type of the object to validate
     * @param object the object to validate
     * @param message the error message if validation fails
     * @return the validated object
     * @throws IllegalArgumentException if the object is null
     */
    private static <T> T validateNotNull(T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }
}