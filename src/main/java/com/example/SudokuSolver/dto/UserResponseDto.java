package com.example.SudokuSolver.dto;


import java.time.LocalDateTime;
public record UserResponseDto(
        Long id,
        String userName,
        String email,
        String imgUrl,
        LocalDateTime creationDate,
        int streak,
        int totalScore
) {}
