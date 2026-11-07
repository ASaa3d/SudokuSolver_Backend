package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyEmailRequestDto(
        @NotBlank String email,
        @NotBlank String code
) {}
