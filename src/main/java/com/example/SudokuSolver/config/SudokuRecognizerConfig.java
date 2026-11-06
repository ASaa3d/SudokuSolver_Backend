package com.example.SudokuSolver.config;

import com.example.SudokuSolver.NativeLibraryLoader;
import com.example.SudokuSolver.ServerSudokuRecognizer;
import com.example.SudokuSolver.sudoku.SudokuRecognizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates one shared recognizer for the whole app (models load once).
 * The ONNX models are read from src/main/resources/models/ and copied to a temp
 * folder, because the native code needs real file paths.
 *
 * If your @SpringBootApplication class is NOT in com.example.SudokuSolver (or a parent
 * package), move this class next to it so it gets component-scanned.
 */
@Configuration
public class SudokuRecognizerConfig {

    @Bean(destroyMethod = "close")
    public SudokuRecognizer sudokuRecognizer() throws IOException {
        Path dir = Files.createTempDirectory("min_sud_models");
        dir.toFile().deleteOnExit();

        Path digit = dir.resolve("cnn_dig.onnx");
        Path yolo = dir.resolve("yolo_sud.onnx");
        NativeLibraryLoader.extractResource("/models/cnn_dig.onnx", digit, true);
        NativeLibraryLoader.extractResource("/models/yolo_sud.onnx", yolo, true);

        return new ServerSudokuRecognizer(digit.toString(), yolo.toString());
    }
}
