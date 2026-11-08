package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidateResetPasswordRequestDto(
        @NotBlank @Email String email,
        @NotBlank String code
) {}
