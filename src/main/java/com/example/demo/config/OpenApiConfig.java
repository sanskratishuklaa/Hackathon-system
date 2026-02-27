package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger UI configuration.
 *
 * Accessible at: http://localhost:8080/swagger-ui.html
 *
 * The "Authorize" button in the UI accepts a Bearer JWT token, which is
 * then sent automatically in the Authorization header for all secured
 * endpoints during API exploration.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI hackNationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HackNation API")
                        .description("National Hackathon Management System — REST API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HackNation Team")
                                .email("support@hacknation.io"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Attach the bearer-auth scheme globally
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter the JWT token obtained from POST /api/auth/login")));
    }
}
