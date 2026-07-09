package com.flashsale.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description Swagger / OpenAPI 3 configuration
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Configuration
public class OpenApiConfig {

    @Bean
    /**
     * @description Configure Swagger OpenAPI metadata (title, description, version)
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Simple Flash Sale API")
                        .description("This is the API documentation for the Simple Flash Sale backend.")
                        .version("v1.0.0"));
    }
}
