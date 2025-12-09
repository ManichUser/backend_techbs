package com.techbs.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Autoriser Swagger UI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/api-docs/**"
                ).permitAll()
                
                // Autoriser les fichiers statiques (images, pdfs, audios, videos)
                .requestMatchers(
                    "/images/**",
                    "/pdfs/**",
                    "/audios/**",
                    "/videos/**"
                ).permitAll()
                
                // Autoriser toutes les API pour le moment (à sécuriser plus tard)
                .requestMatchers("/api/**").permitAll()
                
                // Toutes les autres requêtes nécessitent une authentification
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}