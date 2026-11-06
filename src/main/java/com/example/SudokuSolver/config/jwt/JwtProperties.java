package com.example.SudokuSolver.config.jwt;

import org.springframework.core.io.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String issuer, Duration accessTtl,
                            Resource privateKey, Resource publicKey) {}