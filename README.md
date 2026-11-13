# Sudoku Solver Backend

Spring Boot REST API for Sudoku solving, user accounts, and puzzle-related features.

## API documentation

Explore the interactive Swagger UI: [Sudoku Solver API](https://sudokusolver-backend-83nr.onrender.com/swagger-ui.html).

The API is hosted on Render, where the instance may spin down while idle. The first request after a period of inactivity can take about a minute while the service wakes up.

## Architecture

- **Layered design:** controllers expose REST endpoints, services hold business logic, repositories access PostgreSQL through Spring Data JPA, and DTOs keep API payloads separate from persistence entities. MapStruct handles user mapping.
- **Sudoku recognition:** a shared service loads the bundled ONNX models and calls the native Sudoku library to process image data.
- **Main stack:** Java 21, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, and Gradle.

## Native C++ integration

The Sudoku recognition engine is provided by the Linux x64 `libmin_sud_core.so` shared library, built from the companion C++/CMake project. Java connects to it through JNI: `MinSud` declares the native methods for creating and destroying a recognizer and processing an image file or pixel bytes. The byte interface passes dimensions, pixel format, and row stride, and returns the recognized grid as an `int[][]`.

`SudokuRecognizerConfig` creates one shared recognizer so the ONNX models are loaded once. `NativeLibraryLoader` extracts the packaged `.so` and `libonnxruntime.so` to a temporary directory, loads ONNX Runtime first, then loads the core library. The wrapper manages the native handle and protects processing against concurrent close. The Docker runtime includes `libgomp1`; the bundled native binaries currently target Linux x64.

## Containerization

The multi-stage `Dockerfile` builds the application with a Java 21 JDK, then runs the packaged JAR on a Java 21 JRE image. The runtime installs `libgomp1` for the bundled native library and runs as an unprivileged `spring` user. The API listens on port `8080`.

Build the image from the repository root:

```bash
docker build -t sudoku-solver-backend .
```

Provide database, mail, frontend URL, and JWT key settings through the environment when running the container. Keep production credentials and private keys out of the image and source control.

## Authentication and tokens

- Login verifies passwords with Spring Security's delegating password encoder (BCrypt by default) and returns a short-lived access JWT. Protected routes use `Authorization: Bearer <token>`; the API is stateless.
- JWTs are **signed, not encrypted**. The server signs using RSA RS256 and the private key; Spring Security validates the signature with the public key. JWT claims are Base64URL-encoded, not secret—do not put sensitive data in them. There is no JWT decryption step.
- Password reset and account verification use separate cryptographically random six-digit codes that expire after 10 minutes. A newly issued code replaces the previous code of the same type.
- Changing a password invalidates previously issued access JWTs using the password-change timestamp.

Configure key paths and token lifetime with the `app.jwt` settings in `application.yml`. Configure database, SMTP, and frontend settings through the corresponding application properties and environment variables.

## Related projects

- Web frontend: [Repository](https://github.com/your-username/your-web-frontend-repo)
- Android app: [Repository](https://github.com/your-username/your-android-app-repo)
- C++/CMake native Sudoku library: [Repository](https://github.com/your-username/your-cpp-cmake-repo)
