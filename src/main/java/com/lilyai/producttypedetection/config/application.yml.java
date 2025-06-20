# Application Configuration for Product Type Detection Service
# Spring Boot 3.4.0 with Java 23

spring:
  application:
    name: product-type-detection-service
    version: '@project.version@'
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  # Database Configuration
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/product_detection}
    username: ${DATABASE_USERNAME:product_user}
    password: ${DATABASE_PASSWORD:product_pass}
    driver-class-name: org.postgresql.Driver
    hikari:
      connection-timeout: ${DB_CONNECTION_TIMEOUT:20000}
      idle-timeout: ${DB_IDLE_TIMEOUT:300000}
      max-lifetime: ${DB_MAX_LIFETIME:1200000}
      maximum-pool-size: ${DB_MAX_POOL_SIZE:10}
      minimum-idle: ${DB_MIN_IDLE:5}
      pool-name: ProductDetectionHikariCP
      leak-detection-threshold: ${DB_LEAK_DETECTION_THRESHOLD:60000}
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  # Liquibase Configuration
  liquibase:
    change-log: ${LIQUIBASE_CHANGELOG:classpath:db/changelog/db.changelog-master.xml}
    contexts: ${LIQUIBASE_CONTEXTS:default}
    default-schema: ${LIQUIBASE_DEFAULT_SCHEMA:public}
    liquibase-schema: ${LIQUIBASE_SCHEMA:public}
    enabled: ${LIQUIBASE_ENABLED:true}
    drop-first: ${LIQUIBASE_DROP_FIRST:false}
  
  # Cache Configuration
  cache:
    type: ${CACHE_TYPE:redis}
    cache-names:
      - productTypes
      - detectionResults
      - modelPredictions
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: ${REDIS_TIMEOUT:2000ms}
      lettuce:
        pool:
          max-active: ${REDIS_POOL_MAX_ACTIVE:8}
          max-idle: ${REDIS_POOL_MAX_IDLE:8}
          min-idle: ${REDIS_POOL_MIN_IDLE:0}
          max-wait: ${REDIS_POOL_MAX_WAIT:-1ms}
  
  # Jackson Configuration
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: ${JACKSON_INDENT_OUTPUT:false}
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null
    time-zone: UTC
  
  # Web Configuration
  web:
    resources:
      add-mappings: ${WEB_RESOURCES_ADD_MAPPINGS:true}
  
  # Security Configuration
  security:
    require-ssl: ${SECURITY_REQUIRE_SSL:false}

# Server Configuration
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${SERVER_CONTEXT_PATH:/api/v1}
  compression:
    enabled: ${SERVER_COMPRESSION_ENABLED:true}
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
  error:
    include-message: ${SERVER_ERROR_INCLUDE_MESSAGE:on_param}
    include-binding-errors: ${SERVER_ERROR_INCLUDE_BINDING_ERRORS:on_param}
    include-stacktrace: ${SERVER_ERROR_INCLUDE_STACKTRACE:on_param}
    include-exception: ${SERVER_ERROR_INCLUDE_EXCEPTION:false}

# Management Configuration
management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_INCLUDE:health,info,metrics,prometheus}
      base-path: ${MANAGEMENT_BASE_PATH:/actuator}
  endpoint:
    health:
      show-details: ${MANAGEMENT_HEALTH_SHOW_DETAILS:when_authorized}
      show-components: ${MANAGEMENT_HEALTH_SHOW_COMPONENTS:when_authorized}
    metrics:
      enabled: ${MANAGEMENT_METRICS_ENABLED:true}
  metrics:
    export:
      prometheus:
        enabled: ${PROMETHEUS_METRICS_ENABLED:true}
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}

# Logging Configuration
logging:
  level:
    root: ${LOG_LEVEL_ROOT:INFO}
    com.lilyai.producttypedetection: ${LOG_LEVEL_APP:INFO}
    org.springframework: ${LOG_LEVEL_SPRING:INFO}
    org.hibernate: ${LOG_LEVEL_HIBERNATE:WARN}
    org.hibernate.SQL: ${LOG_LEVEL_HIBERNATE_SQL:WARN}
    org.hibernate.type.descriptor.sql.BasicBinder: ${LOG_LEVEL_HIBERNATE_BINDER:WARN}
    liquibase: ${LOG_LEVEL_LIQUIBASE:INFO}
    redis: ${LOG_LEVEL_REDIS:INFO}
  pattern:
    console: ${LOG_PATTERN_CONSOLE:%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx}
    file: ${LOG_PATTERN_FILE:%d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${PID:- } --- [%t] %-40.40logger{39} : %m%n%wEx}
  file:
    name: ${LOG_FILE_NAME:logs/product-type-detection.log}
    max-size: ${LOG_FILE_MAX_SIZE:100MB}
    max-history: ${LOG_FILE_MAX_HISTORY:30}
    total-size-cap: ${LOG_FILE_TOTAL_SIZE_CAP:1GB}

# Application Specific Configuration
app:
  product-detection:
    model:
      endpoint: ${MODEL_ENDPOINT:http://localhost:8081/predict}
      timeout: ${MODEL_TIMEOUT:30s}
      retry:
        max-attempts: ${MODEL_RETRY_MAX_ATTEMPTS:3}
        delay: ${MODEL_RETRY_DELAY:1s}
    cache:
      ttl: ${CACHE_TTL:3600s}
      max-size: ${CACHE_MAX_SIZE:10000}
    batch:
      size: ${BATCH_SIZE:100}
      timeout: ${BATCH_TIMEOUT:300s}

---
# Development Profile
spring:
  config:
    activate:
      on-profile: dev
  
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
    hikari:
      maximum-pool-size: 5
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  liquibase:
    enabled: false
  
  cache:
    type: simple
  
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    root: DEBUG
    com.lilyai.producttypedetection: DEBUG
    org.springframework: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

server:
  error:
    include-stacktrace: always
    include-exception: true

management:
  endpoint:
    health:
      show-details: always
      show-components: always

app:
  product-detection:
    model:
      endpoint: http://localhost:8081/predict
    cache:
      ttl: 300s

---
# Production Profile
spring:
  config:
    activate:
      on-profile: prod
  
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  
  jpa:
    show-sql: false
  
  cache:
    type: redis

logging:
  level:
    root: WARN
    com.lilyai.producttypedetection: INFO
    org.springframework: WARN

server:
  compression:
    enabled: true
  error:
    include-stacktrace: never
    include-exception: false

management:
  endpoint:
    health:
      show-details: when_authorized

app:
  product-detection:
    cache:
      ttl: 7200s
      max-size: 50000
    batch:
      size: 500