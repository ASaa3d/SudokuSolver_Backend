package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.service.PuzzleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/puzzles")
public class PuzzleController {
    private final PuzzleService puzzleService;

    @PostMapping("/{puzzleId}/solve")
    public ResponseEntity<Void> solve(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long puzzleId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        puzzleService.solve(userId, puzzleId);
        return ResponseEntity.noContent().build();
    }
}
