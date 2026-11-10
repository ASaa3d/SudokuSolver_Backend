package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.*;

public interface AuthService {
    TokenResponseDto login(LogInRequestDto request);
    SignUpResponseDto signUp(SignUpRequestDto request);
    void verifyAccount(String email, String code);
    void sendVerificationToken(String email);
    void forgotPassword(String email);
    void resendPasswordChange(String email);
    void validatePasswordResetCode(String email, String code);
    void resetPassword(String email, String code, String newPassword);
}
