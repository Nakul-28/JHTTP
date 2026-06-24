package com.httpserver.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Phase 6/11: thread-safe request logger that appends one line per request.
 */
public class RequestLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;

    public RequestLogger(String logFilePath) throws IOException {
        this(Path.of(logFilePath));
    }

    public RequestLogger(Path logFile) throws IOException {
        this.logFile = logFile.toAbsolutePath().normalize();
        Path parent = this.logFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(this.logFile)) {
            Files.createFile(this.logFile);
        }
    }

    public synchronized void log(String clientIp, String method, String path, int statusCode, long latencyMillis) {
        String line = String.format("%s %s %s %s %d %dms%n",
                LocalDateTime.now().format(FORMATTER),
                clientIp,
                method,
                path,
                statusCode,
                latencyMillis);

        try {
            Files.writeString(logFile, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write request log: " + e.getMessage());
        }
    }

    public synchronized void flush() {
    }

    public Path getLogFile() {
        return logFile;
    }
}