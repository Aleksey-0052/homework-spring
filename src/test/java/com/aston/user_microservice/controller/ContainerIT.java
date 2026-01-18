package com.aston.user_microservice.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test_database")
@Sql(scripts = {"classpath:liquibase/scripts/01-create-table.sql", "classpath:test.sql"},
        config = @SqlConfig(encoding = "UTF-8"))
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class ContainerIT {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void overrideProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("DATASOURCE_URL", POSTGRES::getJdbcUrl);
        registry.add("DATASOURCE_USERNAME", POSTGRES::getUsername);
        registry.add("DATASOURCE_PASSWORD", POSTGRES::getPassword);
    }

}
