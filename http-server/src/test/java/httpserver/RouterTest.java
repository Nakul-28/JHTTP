package com.httpserver.server;

import com.httpserver.cache.CacheManager;
import com.httpserver.http.HttpResponse;
import com.httpserver.metrics.MetricsCollector;
import com.httpserver.routing.ApiHandler;
import com.httpserver.routing.Router;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterTest {

    private static Router router;
    private static MetricsCollector metricsCollector;

    @BeforeAll
    static void setUp() {
        metricsCollector = new MetricsCollector();
        FileHandler fileHandler = new FileHandler("www", new CacheManager(metricsCollector));
        router = new Router(fileHandler, new ApiHandler(metricsCollector));
    }

    @Test
    void apiHealthReturnsJsonResponse() throws IOException {
        HttpResponse response = router.route("/api/health");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.write(out);
        String rawResponse = out.toString(StandardCharsets.UTF_8);

        assertEquals(200, response.getStatusCode());
        assertTrue(rawResponse.contains("{\"status\":\"UP\"}"));
        assertTrue(rawResponse.contains("Content-Type: application/json; charset=utf-8"));
    }

    @Test
    void apiTimeReturnsJsonResponse() throws IOException {
        HttpResponse response = router.route("/api/time");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.write(out);
        String rawResponse = out.toString(StandardCharsets.UTF_8);

        assertEquals(200, response.getStatusCode());
        assertTrue(rawResponse.contains("{\"time\":\""));
        assertTrue(rawResponse.contains("Content-Type: application/json; charset=utf-8"));
    }

    @Test
    void apiStatsReturnsMetricsJson() throws IOException {
        router.route("/index.html");
        HttpResponse response = router.route("/api/stats");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.write(out);
        String rawResponse = out.toString(StandardCharsets.UTF_8);

        assertEquals(200, response.getStatusCode());
        assertTrue(rawResponse.contains("\"requests\":"));
        assertTrue(rawResponse.contains("\"cacheHits\":"));
        assertTrue(rawResponse.contains("\"cacheMisses\":"));
    }

    @Test
    void monitoringEndpointsAreExemptFromRateLimiting() {
        assertTrue(router.isMonitoringEndpoint("/api/time"));
        assertTrue(router.isMonitoringEndpoint("/api/health?probe=true"));
        assertTrue(router.isMonitoringEndpoint("/api/stats"));
        assertFalse(router.isMonitoringEndpoint("/api/unknown"));
        assertFalse(router.isMonitoringEndpoint("/index.html"));
    }

    @Test
    void unknownApiRouteReturns404() {
        HttpResponse response = router.route("/api/unknown");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void nonApiRouteFallsBackToStaticFiles() {
        HttpResponse response = router.route("/index.html");
        assertEquals(200, response.getStatusCode());
    }
}