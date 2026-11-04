package com.example.SudokuSolver.config.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtConfig {
    @Bean
    JwtEncoder jwtEncoder(JwtProperties p) {
        JWK jwk = new RSAKey.Builder(p.publicKey())
                .privateKey(p.privateKey())
                .keyID("key-2026-11")
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties p) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(p.publicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(p.issuer()));
        return decoder;
    }
}
