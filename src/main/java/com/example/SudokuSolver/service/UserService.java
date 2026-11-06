package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.UpdateProfileRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;

public interface UserService {
    UserResponseDto getUser(Long userId);
    UserResponseDto updateProfile(Long userId, UpdateProfileRequestDto request);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
