package com.example.SudokuSolver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequestDto(@NotBlank @Email String email) {}
