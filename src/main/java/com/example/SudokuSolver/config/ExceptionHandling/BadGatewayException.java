package com.example.SudokuSolver.config.ExceptionHandling;

public class BadGatewayException extends RuntimeException {
    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
