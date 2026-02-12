package com.flashsale.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Simple Flash Sale API")
                        .description("This is the API documentation for the Simple Flash Sale backend.")
                        .version("v1.0.0"));
    }
}
