package com.lilyai.producttypedetection.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.boot.actuator.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuator.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuator.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuator.endpoint.ExposableEndpoint;
import org.springframework.boot.actuator.endpoint.web.*;
import org.springframework.boot.actuator.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuator.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuator.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootApplication(scanBasePackages = "com.lilyai.producttypedetection")
@EnableJpaRepositories(basePackages = "com.lilyai.producttypedetection.repository")
@EntityScan(basePackages = "com.lilyai.producttypedetection.entity")
@EnableCaching
@EnableTransactionManagement
@Validated
public class DependencyConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(DependencyConfiguration.class);
    
    @Bean
    public CacheManager cacheManager() {
        logger.info("Configuring cache manager with Java 23 features");
        return new ConcurrentMapCacheManager("productTypes", "detectionResults");
    }
    
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
            WebEndpointsSupplier webEndpointsSupplier,
            ServletEndpointsSupplier servletEndpointsSupplier,
            ControllerEndpointsSupplier controllerEndpointsSupplier,
            EndpointMediaTypes endpointMediaTypes,
            CorsEndpointProperties corsProperties,
            WebEndpointProperties webEndpointProperties,
            Environment environment) {
        
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = shouldRegisterLinksMapping(
                webEndpointProperties, environment, basePath);
        
        return new WebMvcEndpointHandlerMapping(
                endpointMapping, webEndpoints, endpointMediaTypes,
                corsProperties.toCorsConfiguration(),
                new EndpointLinksResolver(allEndpoints, basePath),
                shouldRegisterLinksMapping, null);
    }
    
    private boolean shouldRegisterLinksMapping(
            WebEndpointProperties webEndpointProperties,
            Environment environment, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() &&
               (StringUtils.hasText(basePath) ||
                ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }
    
    @RestController
    @RequestMapping("/api/v1/health")
    public static class HealthController {
        
        private static final Logger log = LoggerFactory.getLogger(HealthController.class);
        
        @GetMapping("/status")
        public HealthStatus getHealthStatus() {
            log.debug("Health check requested using Java 23 features");
            return new HealthStatus("UP", "Product Type Detection Service is running");
        }
    }
    
    public record HealthStatus(
            @NotBlank String status,
            @NotNull String message
    ) {
        public HealthStatus {
            logger.trace("Creating health status record: {} - {}", status, message);
        }
    }
}