package com.epam.gymmanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "spring.jpa.hibernate",
        name = "ddl-auto",
        havingValue = "create-drop"
)
public class SpringSessionSchemaInitializer implements ApplicationRunner {

    private static final String SPRING_SESSION_SCHEMA_PATH =
            "db/session/postgres-spring-session-schema.sql";

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing Spring Session schema from SQL file");

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource(SPRING_SESSION_SCHEMA_PATH));
        databasePopulator.setSeparator(";");
        databasePopulator.setContinueOnError(false);
        databasePopulator.setSqlScriptEncoding("UTF-8");

        databasePopulator.execute(dataSource);

        log.info("Spring Session schema initialized successfully");
    }
}