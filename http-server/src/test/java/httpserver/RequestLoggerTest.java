package com.httpserver.server;

import com.httpserver.logging.RequestLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLoggerTest {

    @TempDir
    Path tempDir;

    @Test
    void writesRequestDetailsToLogFile() throws IOException {
        Path logFile = tempDir.resolve("server.log");
        RequestLogger requestLogger = new RequestLogger(logFile);

        requestLogger.log("127.0.0.1", "GET", "/index.html", 200, 4);

        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("127.0.0.1 GET /index.html 200 4ms"));
    }
}