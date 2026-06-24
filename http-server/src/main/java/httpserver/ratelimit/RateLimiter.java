package com.httpserver.ratelimit;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Phase 8: fixed-window rate limiter keyed by client IP.
 */
public class RateLimiter {

    private final ConcurrentMap<String, RequestWindow> windows = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;

    public RateLimiter(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    public RateLimiter(int maxRequests, long windowMillis, Clock clock) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be greater than zero");
        }
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be greater than zero");
        }
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean allow(String clientIp) {
        long now = clock.millis();
        RequestWindow window = windows.compute(clientIp, (key, existing) -> updateWindow(existing, now));
        return window.requestCount() <= maxRequests;
    }

    private RequestWindow updateWindow(RequestWindow existing, long now) {
        if (existing == null || now - existing.windowStartMillis() >= windowMillis) {
            return new RequestWindow(now, 1);
        }
        return new RequestWindow(existing.windowStartMillis(), existing.requestCount() + 1);
    }

    private record RequestWindow(long windowStartMillis, int requestCount) {
    }
}