package com.httpserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a parsed HTTP request.
 *
 * Phase 1/2: parses the request line (method, path, version) and headers.
 * Body parsing is intentionally left out for now since Phase 1 only needs GET.
 */
public class HttpRequest {

    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;

    private HttpRequest(String method, String path, String version, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
    }

    /**
     * Reads and parses an HTTP request from the given reader.
     *
     * @param reader buffered reader wrapping the socket's input stream
     * @return a parsed HttpRequest, or null if the client closed the connection
     *         before sending a request line (e.g. an empty/idle connection).
     */
    public static HttpRequest parse(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            return null;
        }

        // Example request line: "GET /index.html HTTP/1.1"
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Malformed request line: " + requestLine);
        }

        String method = parts[0];
        String path = parts[1];
        String version = parts[2];

        Map<String, String> headers = new LinkedHashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isBlank()) {
            int separator = line.indexOf(':');
            if (separator > 0) {
                String name = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                headers.put(name, value);
            }
        }

        return new HttpRequest(method, path, version, headers);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public String toString() {
        return method + " " + path + " " + version;
    }
}
