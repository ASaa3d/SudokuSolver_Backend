package com.example.SudokuSolver;

import com.example.SudokuSolver.sudoku.PixelFormat;
import com.example.SudokuSolver.sudoku.SudokuRecognizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


public class ServerSudokuRecognizer implements SudokuRecognizer {

    private final MinSud nativeSud;

    public ServerSudokuRecognizer(String digitModelPath, String yoloModelPath) {
        this.nativeSud = new MinSud(digitModelPath, yoloModelPath);
    }

    @Override
    public int[][] process(byte[] data, int width, int height, PixelFormat format, int rowStride) {
        return nativeSud.process(data, width, height, format, rowStride);
    }

    @Override
    public int[][] processFile(String path) {
        return nativeSud.process(path);
    }

    /**
     * Server-side shortcut for when you already have a decoded image (e.g. from an upload),
     * the equivalent of the Android Bitmap overload.
     *
     * @param image the PixelFormat entry that means 3 bytes per pixel in B,G,R order
     */
    @Override
    public int[][] process(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage bgr = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = bgr.createGraphics();
        g.setColor(Color.WHITE);              // transparent PNGs would otherwise become black
        g.fillRect(0, 0, w, h);
        g.drawImage(image, 0, 0, null);
        g.dispose();

        byte[] data = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();
        return nativeSud.process(data, w, h, PixelFormat.BGR8, w * 3);   // your enum entry with code 2
    }

    @Override
    public void close() {
        nativeSud.close();
    }
}
