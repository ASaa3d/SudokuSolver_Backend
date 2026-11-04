package com.example.SudokuSolver.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String issuer, Duration accessTtl,
                            RSAPrivateKey privateKey, RSAPublicKey publicKey) {}
