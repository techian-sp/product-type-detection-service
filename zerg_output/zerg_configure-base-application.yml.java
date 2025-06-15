```yaml
# application.yml

spring:
  profiles:
    active: dev

  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  liquibase:
    change-log: classpath:/db/changelog/db.changelog-master.yaml
    contexts: ${LIQUIBASE_CONTEXT:dev}

  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterAccess=600s

  logging:
    level:
      root: ${LOG_LEVEL:INFO}
      org.springframework: ${SPRING_LOG_LEVEL:DEBUG}

---
spring:
  profiles: dev

  datasource:
    url: jdbc:mysql://localhost:3306/devdb
    username: ${DEV_DB_USERNAME:devuser}
    password: ${DEV_DB_PASSWORD:devpass}

  liquibase:
    contexts: dev

  logging:
    level:
      root: DEBUG

---
spring:
  profiles: prod

  datasource:
    url: jdbc:mysql://prod-db-server:3306/proddb
    username: ${PROD_DB_USERNAME:produser}
    password: ${PROD_DB_PASSWORD:prodpass}

  liquibase:
    contexts: prod

  logging:
    level:
      root: WARN
```