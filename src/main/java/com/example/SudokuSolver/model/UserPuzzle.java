package com.example.SudokuSolver.model;


import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.LocalDateTime;


@Table(name = "user_puzzle")
public record UserPuzzle (
    @Column(name = "puzzle_id") Long puzzleId,
    LocalDateTime solvedAt
){}
