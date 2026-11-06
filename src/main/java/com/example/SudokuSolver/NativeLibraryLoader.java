package com.example.SudokuSolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads libmin_sud_core.so (and libonnxruntime.so next to it).
 *
 * By default the libraries are read from the classpath under /native/linux-x64/
 * (so they travel inside the Spring Boot jar) and extracted to a temp folder.
 * To load from a real folder instead (e.g. in Docker), start the app with
 *   -Dminsud.native.dir=/opt/min_sud
 */
public final class NativeLibraryLoader {

    private static final String RESOURCE_DIR = "/native/linux-x64/";
    private static final String CORE_LIB = "libmin_sud_core.so";
    private static final String ORT_LIB = "libonnxruntime.so";

    private static boolean loaded = false;

    private NativeLibraryLoader() {}

    public static synchronized void load() {
        if (loaded) return;

        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();
        if (!os.contains("linux") || !(arch.equals("amd64") || arch.equals("x86_64"))) {
            throw new UnsatisfiedLinkError("min_sud native library is built for linux x64 only, got "
                    + os + "/" + arch);
        }

        try {
            Path dir;
            String overrideDir = System.getProperty("minsud.native.dir");
            if (overrideDir != null && !overrideDir.isBlank()) {
                dir = Path.of(overrideDir);
            } else {
                dir = Files.createTempDirectory("min_sud_native");
                dir.toFile().deleteOnExit();
                extractResource(RESOURCE_DIR + ORT_LIB, dir.resolve(ORT_LIB), false);
                extractResource(RESOURCE_DIR + CORE_LIB, dir.resolve(CORE_LIB), true);
            }

            // ONNX Runtime first (the core lib also finds it through $ORIGIN rpath).
            Path ort = dir.resolve(ORT_LIB);
            if (Files.exists(ort)) {
                System.load(ort.toAbsolutePath().toString());
            }
            System.load(dir.resolve(CORE_LIB).toAbsolutePath().toString());
            loaded = true;
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Could not extract native libraries: " + e);
        }
    }

    /**
     * Copies a classpath resource to a file. Returns false if the resource is
     * missing and not required.
     */
    public static boolean extractResource(String resource, Path target, boolean required) throws IOException {
        try (InputStream in = NativeLibraryLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                if (required) throw new IOException("Missing classpath resource: " + resource);
                return false;
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            target.toFile().deleteOnExit();
            return true;
        }
    }
}
