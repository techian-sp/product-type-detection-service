spring:
  application:
    name: product-type-detection
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/product_detection}
    username: ${DATABASE_USERNAME:product_user}
    password: ${DATABASE_PASSWORD:product_pass}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: ${DB_MIN_IDLE:5}
      connection-timeout: ${DB_CONNECTION_TIMEOUT:30000}
      idle-timeout: ${DB_IDLE_TIMEOUT:600000}
      max-lifetime: ${DB_MAX_LIFETIME:1800000}
      leak-detection-threshold: ${DB_LEAK_DETECTION:60000}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: ${JPA_BATCH_SIZE:25}
        order_inserts: true
        order_updates: true
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    contexts: ${LIQUIBASE_CONTEXTS:default}
    drop-first: false
    enabled: ${LIQUIBASE_ENABLED:true}
  
  cache:
    type: redis
    redis:
      time-to-live: ${CACHE_TTL:3600000}
      cache-null-values: false
      key-prefix: "product-detection:"
      use-key-prefix: true
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: ${REDIS_TIMEOUT:2000}
      lettuce:
        pool:
          max-active: ${REDIS_POOL_MAX_ACTIVE:8}
          max-idle: ${REDIS_POOL_MAX_IDLE:8}
          min-idle: ${REDIS_POOL_MIN_IDLE:0}
          max-wait: ${REDIS_POOL_MAX_WAIT:-1}

logging:
  level:
    com.lilyai.producttypedetection: ${LOG_LEVEL_APP:INFO}
    org.springframework: ${LOG_LEVEL_SPRING:WARN}
    org.hibernate: ${LOG_LEVEL_HIBERNATE:WARN}
    liquibase: ${LOG_LEVEL_LIQUIBASE:INFO}
    redis: ${LOG_LEVEL_REDIS:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE:logs/product-detection.log}
    max-size: ${LOG_FILE_MAX_SIZE:100MB}
    max-history: ${LOG_FILE_MAX_HISTORY:30}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: ${HEALTH_SHOW_DETAILS:when-authorized}
  metrics:
    export:
      prometheus:
        enabled: ${PROMETHEUS_ENABLED:true}

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${CONTEXT_PATH:/api/v1}
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

---
spring:
  config:
    activate:
      on-profile: dev
  
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  data:
    redis:
      host: localhost
      port: 6379
      database: 1

logging:
  level:
    com.lilyai.producttypedetection: DEBUG
    org.springframework: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

---
spring:
  config:
    activate:
      on-profile: prod
  
  jpa:
    show-sql: false
    properties:
      hibernate:
        generate_statistics: false
        jdbc:
          batch_size: 50
  
  liquibase:
    contexts: production

logging:
  level:
    com.lilyai.producttypedetection: INFO
    org.springframework: WARN
    org.hibernate: WARN
    liquibase: WARN
  file:
    name: /var/log/product-detection/application.log

server:
  forward-headers-strategy: framework
  tomcat:
    remoteip:
      remote-ip-header: x-forwarded-for
      protocol-header: x-forwarded-proto