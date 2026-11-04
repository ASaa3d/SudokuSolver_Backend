package com.example.SudokuSolver.repository;

import com.example.SudokuSolver.model.TokenType;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long> {
    Optional<VerificationToken> findTopByUserAndTypeAndUsedAtIsNullOrderByExpiresAtDesc(User user, TokenType type);
    void deleteByUserAndType(User user, TokenType type);
}
