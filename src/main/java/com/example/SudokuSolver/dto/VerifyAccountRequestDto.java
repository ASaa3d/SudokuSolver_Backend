package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyAccountRequestDto(
        @NotNull Long userId,
        @NotBlank String code
) {}
