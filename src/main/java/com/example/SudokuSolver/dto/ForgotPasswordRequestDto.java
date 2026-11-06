package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(@NotBlank String emailOrPhone) {}
