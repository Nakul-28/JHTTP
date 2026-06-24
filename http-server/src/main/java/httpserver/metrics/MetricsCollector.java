package com.httpserver.metrics;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Phase 10: collects lightweight runtime metrics for the server.
 */
public class MetricsCollector {

    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final AtomicLong totalLatencyMillis = new AtomicLong();
    private final AtomicInteger activeConnections = new AtomicInteger();

    public void recordRequest(long latencyMillis, int statusCode) {
        totalRequests.incrementAndGet();
        totalLatencyMillis.addAndGet(latencyMillis);
        if (statusCode >= 400) {
            errorCount.incrementAndGet();
        }
    }

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public void connectionOpened() {
        activeConnections.incrementAndGet();
    }

    public void connectionClosed() {
        activeConnections.updateAndGet(current -> current > 0 ? current - 1 : 0);
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getCacheHits() {
        return cacheHits.get();
    }

    public long getCacheMisses() {
        return cacheMisses.get();
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public long getAverageLatencyMillis() {
        long requests = totalRequests.get();
        if (requests == 0) {
            return 0;
        }
        return totalLatencyMillis.get() / requests;
    }

    public String toJson() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long totalCacheLookups = hits + misses;
        double hitRate = totalCacheLookups == 0 ? 0.0 : (hits * 100.0) / totalCacheLookups;

        return String.format(Locale.US,
                "{\"requests\":%d,\"cacheHits\":%d,\"cacheMisses\":%d,\"cacheHitRate\":\"%.1f%%\",\"activeConnections\":%d,\"errorCount\":%d,\"avgLatencyMs\":%d}",
                getTotalRequests(),
                hits,
                misses,
                hitRate,
                getActiveConnections(),
                getErrorCount(),
                getAverageLatencyMillis());
    }
}