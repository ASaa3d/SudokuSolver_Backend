package com.example.SudokuSolver.config.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
@Configuration
public class JwtConfig {

    @Bean
    RSAPublicKey rsaPublicKey(JwtProperties props) throws IOException {
        try (InputStream in = props.publicKey().getInputStream()) {
            return RsaKeyConverters.x509().convert(in);
        }
    }

    @Bean
    RSAPrivateKey rsaPrivateKey(JwtProperties props) throws IOException {
        try (InputStream in = props.privateKey().getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(in);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(RSAPublicKey pub, RSAPrivateKey priv) {
        RSAKey jwk = new RSAKey.Builder(pub).privateKey(priv).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    JwtDecoder jwtDecoder(RSAPublicKey pub) {
        return NimbusJwtDecoder.withPublicKey(pub).build();
    }
}
