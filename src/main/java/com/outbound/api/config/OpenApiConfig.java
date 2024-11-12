package com.outbound.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "customAuth"; // Changed from "bearerAuth" to "customAuth"
        final String customHeaderName = "Authorization";
        return new OpenAPI()
                .info(new Info().title("Backend Service APIs for Supporting OpenAI and LLM chat bot")
                        .version("1.0")
                        .description("Spring WebFlux application with OpenAPI communication with backend MongoDB and OpenAI services."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)
                                                .name(customHeaderName)
                                )
                );
    }
}