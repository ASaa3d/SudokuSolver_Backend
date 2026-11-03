package com.example.SudokuSolver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/**").permitAll() // Allow open access to specific paths
                        .anyRequest().authenticated()             // Lock down everything else
                )
                .formLogin(Customizer.withDefaults())          // Enable standard form login
                .httpBasic(Customizer.withDefaults());         // Enable basic HTTP auth for APIs

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Uses BCrypt by default and formats hashes as {id}encodedPassword
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
