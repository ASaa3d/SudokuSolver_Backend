package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.dto.ChangePasswordRequestDto;
import com.example.SudokuSolver.dto.UpdateProfileRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateProfileRequestDto request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ChangePasswordRequestDto request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        userService.changePassword(userId, request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
