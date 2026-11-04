package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;

public record LogInRequestDto(
        @NotBlank String emailOrPhone,
        @NotBlank String password
) {}