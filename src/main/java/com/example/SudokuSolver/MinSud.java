package com.example.SudokuSolver;

import com.example.SudokuSolver.sudoku.PixelFormat;

import java.io.Closeable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sudoku recognizer. Models are loaded once in the constructor; reuse the instance.
 * close() waits for in-flight process*() calls to finish.
 *
 * NOTE: this class must stay in package com.example.SudokuSolver with the name
 * MinSud, because the native function names (Java_com_example_SudokuSolver_MinSud_*)
 * are derived from them.
 */
public final class MinSud implements Closeable {

    static {
        NativeLibraryLoader.load();
    }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long handle;

    public MinSud(String digitModelPath, String yoloModelPath) {
        this.handle = nativeCreate(digitModelPath, yoloModelPath);
        if (handle == 0L) {
            throw new IllegalStateException(
                    "Failed to load models: " + digitModelPath + " / " + yoloModelPath);
        }
    }

    /** Returns an NxN board (N = 9 or 6, 0 = empty cell), or null if no grid was found. */
    public int[][] process(String imagePath) {
        lock.readLock().lock();
        try {
            return nativeProcessFile(requireOpen(), imagePath);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int[][] process(byte[] data, int width, int height, PixelFormat format) {
        return process(data, width, height, format, 0);
    }

    public int[][] process(byte[] data, int width, int height, PixelFormat format, int rowStride) {
        lock.readLock().lock();
        try {
            return nativeProcessBytes(requireOpen(), data, width, height, format.getCode(), rowStride);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void close() {
        lock.writeLock().lock();
        try {
            if (handle != 0L) {
                nativeDestroy(handle);
                handle = 0L;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private long requireOpen() {
        if (handle == 0L) throw new IllegalStateException("MinSud is closed");
        return handle;
    }

    private native long nativeCreate(String digitModelPath, String yoloModelPath);

    private native void nativeDestroy(long handle);

    private native int[][] nativeProcessFile(long handle, String path);

    private native int[][] nativeProcessBytes(
            long handle, byte[] data, int width, int height, int format, int rowStride);
}
