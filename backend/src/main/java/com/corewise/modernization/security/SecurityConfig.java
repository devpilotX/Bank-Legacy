package com.corewise.modernization.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Locks down the API.
 *
 * Every request needs a logged-in caller except the public liveness check at
 * /health. We will build the real sign-in (token based) when we add the login
 * flow. Until then the default Spring user is enough to keep the rest shut.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/health/**").permitAll()
                .anyRequest().authenticated())
            // This is a stateless API, so there is no browser session to guard with a
            // CSRF token yet. We turn CSRF off for now and will revisit it when we
            // design the real sign-in flow.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(withDefaults())
            .build();
    }
}
