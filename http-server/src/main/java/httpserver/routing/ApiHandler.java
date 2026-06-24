package com.httpserver.routing;

import com.httpserver.http.HttpResponse;
import com.httpserver.metrics.MetricsCollector;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Phase 9/10: handlers for dynamic API endpoints.
 */
public class ApiHandler {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MetricsCollector metricsCollector;

    public ApiHandler(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    public HttpResponse handle(String path) {
        if ("/api/time".equals(path)) {
            return jsonResponse("{\"time\":\"" + LocalTime.now().format(TIME_FORMATTER) + "\"}");
        }
        if ("/api/health".equals(path)) {
            return jsonResponse("{\"status\":\"UP\"}");
        }
        if ("/api/stats".equals(path)) {
            return jsonResponse(metricsCollector.toJson());
        }
        return HttpResponse.notFound();
    }

    private HttpResponse jsonResponse(String json) {
        return HttpResponse.ok(json.getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
    }
}