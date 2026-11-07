package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record ForgotPasswordRequestDto(@NotBlank @Email String email) {}
