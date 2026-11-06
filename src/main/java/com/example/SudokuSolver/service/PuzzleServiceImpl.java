package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.NotFoundException;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PuzzleServiceImpl implements PuzzleService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void solve(Long userId, Long puzzleId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + userId)
        );
        user.solvePuzzle(puzzleId);
    }
}
