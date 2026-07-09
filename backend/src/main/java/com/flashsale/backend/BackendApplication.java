package com.flashsale.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * @description Spring Boot application entry point for Simple-Flash-Sale backend
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class BackendApplication {
    /**
     * @description Bootstrap the Spring Boot application
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
