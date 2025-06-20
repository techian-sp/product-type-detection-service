package com.lilyai.producttypedetection.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

/**
 * Liquibase configuration for Product Type Detection Service.
 * <p>
 * This configuration class sets up Liquibase for database schema management
 * with proper error handling and logging. It includes the master changelog
 * which references product and product-images specific changelogs.
 * </p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Conditional activation based on configuration properties</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Integration with Spring Boot's DataSource configuration</li>
 *   <li>Support for custom changelog locations</li>
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

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseConfig.class);
    
    private static final String DEFAULT_CHANGELOG_PATH = "classpath:db/changelog/db.changelog-master.xml";
    
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
        this.liquibaseProperties = liquibaseProperties != null ? liquibaseProperties : new LiquibaseProperties();
        this.resourceLoader = resourceLoader;
        
        if (resourceLoader == null) {
            throw new IllegalArgumentException("ResourceLoader cannot be null");
        }
        
        logger.info("Initializing Liquibase configuration for Product Type Detection Service");
    }

    /**
     * Creates and configures the SpringLiquibase bean for database migration.
     * <p>
     * This bean is responsible for executing database migrations using the
     * master changelog file which includes product and product-images changelogs.
     * </p>
     *
     * @param dataSource the primary data source for the application
     * @return configured SpringLiquibase instance
     * @throws IllegalArgumentException if dataSource is null
     * @throws RuntimeException if changelog file cannot be found or accessed
     */
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null for Liquibase configuration");
        }
        
        try {
            logger.info("Configuring SpringLiquibase with master changelog");
            
            var springLiquibase = new SpringLiquibase();
            springLiquibase.setDataSource(dataSource);
            
            // Set changelog location with fallback to default
            String changelogPath = liquibaseProperties.getChangeLog();
            if (changelogPath == null || changelogPath.trim().isEmpty()) {
                changelogPath = DEFAULT_CHANGELOG_PATH;
                logger.info("Using default changelog path: {}", changelogPath);
            } else {
                logger.info("Using configured changelog path: {}", changelogPath);
            }
            
            // Verify changelog file exists
            if (!resourceLoader.getResource(changelogPath).exists()) {
                String errorMsg = String.format("Changelog file not found at path: %s", changelogPath);
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            springLiquibase.setChangeLog(changelogPath);
            
            // Configure additional properties
            springLiquibase.setContexts(liquibaseProperties.getContexts());
            springLiquibase.setLabels(liquibaseProperties.getLabels());
            springLiquibase.setDropFirst(liquibaseProperties.isDropFirst());
            springLiquibase.setShouldRun(liquibaseProperties.isEnabled());
            springLiquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
            springLiquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
            
            // Set default schema if specified
            if (liquibaseProperties.getDefaultSchema() != null) {
                springLiquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
            }
            
            // Set liquibase schema if specified
            if (liquibaseProperties.getLiquibaseSchema() != null) {
                springLiquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
            }
            
            logger.info("SpringLiquibase configured successfully");
            return springLiquibase;
            
        } catch (Exception e) {
            logger.error("Failed to configure SpringLiquibase: {}", e.getMessage(), e);
            throw new RuntimeException("Liquibase configuration failed", e);
        }
    }
    
    /**
     * Gets the configured changelog path.
     *
     * @return the changelog path, either from properties or default
     */
    public String getChangelogPath() {
        String changelogPath = liquibaseProperties.getChangeLog();
        return (changelogPath != null && !changelogPath.trim().isEmpty()) 
            ? changelogPath 
            : DEFAULT_CHANGELOG_PATH;
    }
    
    /**
     * Checks if Liquibase is enabled based on configuration.
     *
     * @return true if Liquibase is enabled, false otherwise
     */
    public boolean isLiquibaseEnabled() {
        return liquibaseProperties.isEnabled();
    }
}