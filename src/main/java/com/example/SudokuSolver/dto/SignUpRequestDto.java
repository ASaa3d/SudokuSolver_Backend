package com.example.SudokuSolver.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public record SignUpRequestDto(
        @NotBlank String userName,
        @NotBlank @Email String email,
        Optional<String> phone,
        @NotBlank @Size(min = 8) String password
) {}
