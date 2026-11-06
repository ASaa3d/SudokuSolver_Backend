package com.example.SudokuSolver.sudoku;

import lombok.Getter;

/**
 * Codes MUST match MinSudPixelFormat in sudoku_pipeline.h (the native code switches on them).
 */
@Getter
public enum PixelFormat {
    GRAY8(1),
    BGR8(2),
    RGB8(3),
    RGBA8(4),
    BGRA8(5);

    private final int code;

    PixelFormat(int code) {
        this.code = code;
    }
}