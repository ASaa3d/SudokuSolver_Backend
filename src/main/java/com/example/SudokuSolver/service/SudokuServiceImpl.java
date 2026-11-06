package com.example.SudokuSolver.service;

import com.example.SudokuSolver.sudoku.SudokuRecognizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class SudokuServiceImpl implements SudokuService {
    private final SudokuRecognizer sudokuRecognizer;



    @Override
    public int[][] getGrid(String imgUrl) {
        try {
            BufferedImage img = downloadBufferedImage(imgUrl);
            if (img == null) throw new IOException("Unsupported or corrupt image format");
            return sudokuRecognizer.process(img);
        } catch (Exception e) {
            throw new RuntimeException("COULDN'T READ IMAGE: " + e.getMessage(), e);
        }
    }

    private BufferedImage downloadBufferedImage(String imageUrl) throws IOException {
            URL url = URI.create(imageUrl).toURL();

            // This decodes the full image into memory
        return ImageIO.read(url);
    }
}
