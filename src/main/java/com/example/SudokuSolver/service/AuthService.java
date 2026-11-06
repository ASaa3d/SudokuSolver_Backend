package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.LogInRequestDto;
import com.example.SudokuSolver.dto.SignUpRequestDto;
import com.example.SudokuSolver.dto.TokenResponseDto;
import com.example.SudokuSolver.dto.UserResponseDto;

public interface AuthService {
    TokenResponseDto login(LogInRequestDto request);
    UserResponseDto signUp(SignUpRequestDto request);
    void verifyAccount(Long userId, String code);
    void sendVerificationToken(Long userId);
    void forgotPassword(String emailOrPhone);
    void resetPassword(String emailOrPhone, String newPassword, String code);
}
