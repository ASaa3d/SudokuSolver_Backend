package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.dto.*;
import com.example.SudokuSolver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;


    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LogInRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody @Valid SignUpRequestDto request) {
        SignUpResponseDto user = authService.signUp(request);
        return ResponseEntity.created(URI.create("/api/users/" + user.id())).body(user);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@RequestBody @Valid VerifyEmailRequestDto request) {
        authService.verifyAccount(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestBody @Valid ResendVerificationRequestDto request) {
        authService.sendVerificationToken(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/resend-password-change")
    public ResponseEntity<Void> resendPasswordChange(@RequestBody @Valid ForgotPasswordRequestDto request) {
        authService.resendPasswordChange(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password/validate")
    public ResponseEntity<Void> validateResetPasswordCode(
            @RequestBody @Valid ValidateResetPasswordRequestDto request
    ) {
        authService.validatePasswordResetCode(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        authService.resetPassword(request.email(), request.code(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
