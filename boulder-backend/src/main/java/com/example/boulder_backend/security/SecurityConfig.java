package com.example.boulder_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Holt Secret Key aus yml
    @Value("${jwt.secret}")
    private String jwtSecret;


    /**
     * Konfiguriert  Security-Filter für HTTP-Anfragen
     * - /auth/** ist öffentlich zugänglich (Login, Registrierung)
     * - alle anderen Endpunkte benötigen gültiges JWT-Token
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() //Login & Registrierung öffentlich
                        .anyRequest().authenticated() // alles andere mit JWT
                )
                .csrf(AbstractHttpConfigurer::disable) //deaktiviert CSRF-Schutz
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {

                        })
                );

        return http.build();
    }

    /**
     * Erstellt  JWT Decoder mit geheimen Key.
     * wird zur Verifizierung von eingehenden JWTs verwendet.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Wandelt Secret-String in HMAC-SHA256-Schlüssel um
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}

