package com.flashsale.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.security.AuthJwtFilter;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @author Yang-Hsu
 * @description http config to run filter and allow which url can go in
 * @date 2026/1/8 下午 04:47
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final AuthJwtFilter authJwtFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(AuthJwtFilter authJwtFilter, ObjectMapper objectMapper) {
        this.authJwtFilter = authJwtFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized request intercepted: {}, Error: {}", request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.setContentType("application/json;charset=UTF-8");
                            ApiResponse<Object> apiRes = ApiResponse.of(ResultCode.TOKEN_MISSING);
                            String json = objectMapper.writeValueAsString(apiRes);
                            response.getWriter().write(json);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/client/auth/**").permitAll()
                        .requestMatchers("/api/client/open/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .anyRequest().authenticated()
                );
        //cors : Allow cross-origin requests from specific sites
        //csrf : Disable CSRF since we use JWT (stateless)
        //sessionManagement: Enforce stateless policy, no session will be created
        http.addFilterBefore(authJwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * @description url site setting
     * @author Yang-Hsu
     * @date 2026/1/8 下午 04:54
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); //Remember frontend need to setting
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}