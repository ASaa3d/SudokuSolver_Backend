package com.example.SudokuSolver.sudoku;

import java.awt.image.BufferedImage;

public interface SudokuRecognizer extends AutoCloseable {

    int[][] process(byte[] data, int width, int height, PixelFormat format, int rowStride);

    int[][] processFile(String path);
    int[][] process(BufferedImage bufferedImage);
    @Override
    void close();
}
