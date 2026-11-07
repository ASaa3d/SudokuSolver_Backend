package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.*;

public interface AuthService {
    TokenResponseDto login(LogInRequestDto request);
    SignUpResponseDto signUp(SignUpRequestDto request);
    void verifyAccount(Long userId, String code);
    void sendVerificationToken(Long userId);
    void forgotPassword(String emailOrPhone);
    void resetPassword(String emailOrPhone, String newPassword, String code);
}
