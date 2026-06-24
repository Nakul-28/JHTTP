package com.httpserver.server;

import com.httpserver.cache.CacheManager;
import com.httpserver.logging.RequestLogger;
import com.httpserver.metrics.MetricsCollector;
import com.httpserver.ratelimit.RateLimiter;
import com.httpserver.routing.ApiHandler;
import com.httpserver.routing.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Phase 11: multithreaded HTTP server with graceful shutdown.
 */
public class HttpServer {

    private static final int DEFAULT_PORT = 9000;
    private static final String WEB_ROOT = "www";
    private static final String LOG_FILE = "server.log";
    private static final int THREAD_POOL_SIZE = 8;
    private static final int MAX_REQUESTS_PER_WINDOW = 1_000_000;
    private static final long RATE_LIMIT_WINDOW_MILLIS = 60_000;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        MetricsCollector metricsCollector = new MetricsCollector();
        FileHandler fileHandler = new FileHandler(WEB_ROOT, new CacheManager(metricsCollector));
        Router router = new Router(fileHandler, new ApiHandler(metricsCollector));
        RequestLogger requestLogger = new RequestLogger(LOG_FILE);
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, RATE_LIMIT_WINDOW_MILLIS);
        ThreadPool pool = new ThreadPool(THREAD_POOL_SIZE);
        AtomicBoolean shuttingDown = new AtomicBoolean(false);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
                    shutdownServer(serverSocket, pool, requestLogger, shuttingDown), "http-shutdown"));

            System.out.println("HTTP server listening on http://localhost:" + port);
            System.out.println("Serving static files from: " + WEB_ROOT);
            System.out.println("Custom thread pool size: " + THREAD_POOL_SIZE);
            System.out.println("Writing request logs to: " + requestLogger.getLogFile());
            System.out.println("Rate limit: " + MAX_REQUESTS_PER_WINDOW + " requests per 60 seconds per IP");
            System.out.println("API routes: /api/time, /api/health, /api/stats");

            while (!shuttingDown.get()) {
                try {
                    pool.submit(new ConnectionHandler(serverSocket.accept(), router, requestLogger, rateLimiter, metricsCollector));
                } catch (SocketException e) {
                    if (shuttingDown.get()) {
                        break;
                    }
                    throw e;
                } catch (IllegalStateException e) {
                    if (shuttingDown.get()) {
                        break;
                    }
                    throw e;
                }
            }
        } finally {
            shutdownPoolAndLogger(pool, requestLogger, shuttingDown);
        }
    }

    private static void shutdownServer(ServerSocket serverSocket,
                                       ThreadPool pool,
                                       RequestLogger requestLogger,
                                       AtomicBoolean shuttingDown) {
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }

        System.out.println("Graceful shutdown started...");

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close server socket: " + e.getMessage());
        }

        shutdownPoolAndLogger(pool, requestLogger, shuttingDown);
    }

    private static void shutdownPoolAndLogger(ThreadPool pool,
                                              RequestLogger requestLogger,
                                              AtomicBoolean shuttingDown) {
        pool.shutdown();
        try {
            boolean terminated = pool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!terminated) {
                System.err.println("Thread pool did not terminate within timeout.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        requestLogger.flush();
        if (shuttingDown.get()) {
            System.out.println("Graceful shutdown complete.");
        }
    }
}