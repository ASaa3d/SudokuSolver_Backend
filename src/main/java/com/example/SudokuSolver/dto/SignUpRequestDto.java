package com.example.SudokuSolver.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @NotBlank String userName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
) {}
