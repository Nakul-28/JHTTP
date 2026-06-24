package com.httpserver.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an outgoing HTTP response and knows how to write itself
 * to a socket's output stream.
 */
public class HttpResponse {

    private final int statusCode;
    private final String statusText;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final byte[] body;

    public HttpResponse(int statusCode, String statusText, byte[] body) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.body = body != null ? body : new byte[0];
        headers.put("Content-Length", String.valueOf(this.body.length));
        headers.put("Connection", "close");
    }

    public static HttpResponse ok(byte[] body, String contentType) {
        HttpResponse response = new HttpResponse(200, "OK", body);
        response.setHeader("Content-Type", contentType);
        return response;
    }

    public static HttpResponse notFound() {
        byte[] body = "<html><body><h1>404 Not Found</h1></body></html>"
                .getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(404, "Not Found", body);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        return response;
    }

    public static HttpResponse forbidden() {
        byte[] body = "<html><body><h1>403 Forbidden</h1></body></html>"
                .getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(403, "Forbidden", body);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        return response;
    }

    public static HttpResponse badRequest(String message) {
        byte[] body = ("<html><body><h1>400 Bad Request</h1><p>" + message + "</p></body></html>")
                .getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(400, "Bad Request", body);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        return response;
    }

    public static HttpResponse tooManyRequests() {
        byte[] body = "<html><body><h1>429 Too Many Requests</h1></body></html>"
                .getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(429, "Too Many Requests", body);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        return response;
    }

    public static HttpResponse internalServerError() {
        byte[] body = "<html><body><h1>500 Internal Server Error</h1></body></html>"
                .getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(500, "Internal Server Error", body);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        return response;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Serializes and writes this response to the given output stream,
     * then flushes it. Does not close the stream.
     */
    public void write(OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}