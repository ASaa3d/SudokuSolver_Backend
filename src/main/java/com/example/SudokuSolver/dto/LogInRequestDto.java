package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record LogInRequestDto(
        @NotBlank @Email String email,
        @NotBlank String password
) {}