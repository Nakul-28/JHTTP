package com.httpserver.cache;

import com.httpserver.metrics.MetricsCollector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Phase 7/10: in-memory cache for static file responses with cache metrics.
 */
public class CacheManager {

    private final ConcurrentMap<String, CachedFile> cache = new ConcurrentHashMap<>();
    private final MetricsCollector metricsCollector;

    public CacheManager() {
        this(null);
    }

    public CacheManager(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    public CachedFile get(String path) {
        CachedFile cachedFile = cache.get(path);
        if (metricsCollector != null) {
            if (cachedFile != null) {
                metricsCollector.recordCacheHit();
            } else {
                metricsCollector.recordCacheMiss();
            }
        }
        return cachedFile;
    }

    public void put(String path, CachedFile cachedFile) {
        cache.put(path, cachedFile);
    }

    public boolean contains(String path) {
        return cache.containsKey(path);
    }

    public int size() {
        return cache.size();
    }
}