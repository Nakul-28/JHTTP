package com.httpserver.server;

import com.httpserver.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void allowsRequestsUpToConfiguredLimit() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-25T00:00:00Z"));
        RateLimiter rateLimiter = new RateLimiter(2, 60_000, clock);

        assertTrue(rateLimiter.allow("127.0.0.1"));
        assertTrue(rateLimiter.allow("127.0.0.1"));
        assertFalse(rateLimiter.allow("127.0.0.1"));
    }

    @Test
    void resetsAllowanceAfterWindowExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-25T00:00:00Z"));
        RateLimiter rateLimiter = new RateLimiter(1, 60_000, clock);

        assertTrue(rateLimiter.allow("127.0.0.1"));
        assertFalse(rateLimiter.allow("127.0.0.1"));

        clock.advanceMillis(60_000);

        assertTrue(rateLimiter.allow("127.0.0.1"));
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        private MutableClock(Instant currentInstant) {
            this.currentInstant = currentInstant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }

        void advanceMillis(long millis) {
            currentInstant = currentInstant.plusMillis(millis);
        }
    }
}