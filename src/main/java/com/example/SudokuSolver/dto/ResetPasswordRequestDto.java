package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank String emailOrPhone,
        @NotBlank @Size(min = 8) String newPassword,
        @NotBlank String code
) {}
