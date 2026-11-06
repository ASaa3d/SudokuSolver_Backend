package com.example.SudokuSolver.dto;

public record UpdateProfileRequestDto(
        String userName,
        String imgUrl,
        String phone
) {}
