package com.example.SudokuSolver.dto;


import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record SignUpRequestDto(
        @NotBlank String userName,
        @NotBlank String email,
        Optional<String> phone,
        @NotBlank String password
) {}
