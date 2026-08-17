package com.epam.gymmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPI30Configuration {

    @Bean
    public OpenAPI gymManagementOpenApi(
            @Value("${app.openapi.gym-server-url:/}") String gymServerUrl,
            @Value("${app.openapi.workload-server-url:/workload}") String workloadServerUrl
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym Management Backend API")
                        .version("1.0.0")
                        .description("""
                                Custom OpenAPI documentation for the Gym Management microservices.
                                The API manages authentication, trainees, trainers, assignments, and
                                training sessions while synchronizing monthly trainer workload.
                                """)
                        .contact(new Contact().name("Gym Management API Team"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url(gymServerUrl)
                                .description("Gym Management microservice"),
                        new Server()
                                .url(workloadServerUrl)
                                .description("Trainer Workload microservice")
                ))
                .components(new Components().addSecuritySchemes(
                        "Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT returned by POST /api/v1/auth/login")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Microservices setup and runbook")
                        .url("https://github.com/Matlab28/gym-management"));
    }
}
