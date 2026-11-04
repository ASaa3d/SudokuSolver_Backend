package com.example.SudokuSolver.config.ExceptionHandling;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
