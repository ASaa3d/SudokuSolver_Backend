package com.example.SudokuSolver.dto;


import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record UserRequestDto(
        @NotBlank String userName,
        Optional<String> email,
        Optional<String> phone,
        @NotBlank String password
) {}
