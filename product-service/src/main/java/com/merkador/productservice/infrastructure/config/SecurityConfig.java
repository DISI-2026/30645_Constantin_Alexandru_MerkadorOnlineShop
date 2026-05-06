package com.merkador.productservice.infrastructure.config;

import com.merkador.productservice.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/uploads/products/**").permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/v1/products/**",
                        "/products/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/v1/categories/**",
                        "/products/v1/categories/**").permitAll()

                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                .requestMatchers("/v1/categories/**", "/products/v1/categories/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,
                        "/v1/products/**",
                        "/products/v1/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT,
                        "/v1/products/**",
                        "/products/v1/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.DELETE,
                        "/v1/products/**",
                        "/products/v1/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.PATCH,
                        "/v1/products/**",
                        "/products/v1/products/**").hasRole("SELLER")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
