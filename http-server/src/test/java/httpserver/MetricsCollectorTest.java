package com.httpserver.server;

import com.httpserver.metrics.MetricsCollector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsCollectorTest {

    @Test
    void jsonIncludesTrackedMetrics() {
        MetricsCollector metricsCollector = new MetricsCollector();

        metricsCollector.connectionOpened();
        metricsCollector.recordCacheHit();
        metricsCollector.recordCacheMiss();
        metricsCollector.recordRequest(10, 200);
        metricsCollector.recordRequest(20, 404);

        String json = metricsCollector.toJson();

        assertTrue(json.contains("\"requests\":2"));
        assertTrue(json.contains("\"cacheHits\":1"));
        assertTrue(json.contains("\"cacheMisses\":1"));
        assertTrue(json.contains("\"activeConnections\":1"));
        assertTrue(json.contains("\"errorCount\":1"));
        assertTrue(json.contains("\"avgLatencyMs\":15"));
    }
}