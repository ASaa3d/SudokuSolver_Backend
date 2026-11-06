package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.dto.ForgotPasswordRequestDto;
import com.example.SudokuSolver.dto.LogInRequestDto;
import com.example.SudokuSolver.dto.ResetPasswordRequestDto;
import com.example.SudokuSolver.dto.SignUpRequestDto;
import com.example.SudokuSolver.dto.TokenResponseDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.dto.VerifyAccountRequestDto;
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
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LogInRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody @Valid SignUpRequestDto request) {
        UserResponseDto user = authService.signUp(request);
        return ResponseEntity.created(URI.create("/api/users/" + user.id())).body(user);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@RequestBody @Valid VerifyAccountRequestDto request) {
        authService.verifyAccount(request.userId(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification/{userId}")
    public ResponseEntity<Void> resendVerification(@PathVariable Long userId) {
        authService.sendVerificationToken(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto request) {
        authService.forgotPassword(request.emailOrPhone());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        authService.resetPassword(request.emailOrPhone(), request.newPassword(), request.code());
        return ResponseEntity.noContent().build();
    }
}
