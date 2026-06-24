package com.httpserver.server;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadPoolTest {

    @Test
    void submittedTasksAreExecutedByWorkers() throws InterruptedException {
        ThreadPool pool = new ThreadPool(2);
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger();

        try {
            for (int index = 0; index < 3; index++) {
                pool.submit(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });
            }

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(3, counter.get());
        } finally {
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    @Test
    void shutdownRejectsNewTasksAndTerminatesWorkers() throws InterruptedException {
        ThreadPool pool = new ThreadPool(1);

        pool.shutdown();

        assertFalse(pool.isRunning());
        assertThrows(IllegalStateException.class, () -> pool.submit(() -> { }));
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
    }
}