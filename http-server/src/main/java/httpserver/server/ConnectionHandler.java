package com.httpserver.server;

import com.httpserver.http.HttpRequest;
import com.httpserver.http.HttpResponse;
import com.httpserver.logging.RequestLogger;
import com.httpserver.metrics.MetricsCollector;
import com.httpserver.ratelimit.RateLimiter;
import com.httpserver.routing.Router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Handles a single client connection end-to-end: read the request,
 * resolve a response, write it back, log it, then close the socket.
 */
public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private final Router router;
    private final RequestLogger requestLogger;
    private final RateLimiter rateLimiter;
    private final MetricsCollector metricsCollector;

    public ConnectionHandler(Socket clientSocket,
                             Router router,
                             RequestLogger requestLogger,
                             RateLimiter rateLimiter,
                             MetricsCollector metricsCollector) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.requestLogger = requestLogger;
        this.rateLimiter = rateLimiter;
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void run() {
        long startNanos = System.nanoTime();
        metricsCollector.connectionOpened();

        try (Socket socket = clientSocket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                OutputStream out = socket.getOutputStream()) {

            HttpRequest request = HttpRequest.parse(reader);
            if (request == null) {
                return;
            }

            String clientIp = socket.getInetAddress().getHostAddress();
            HttpResponse response = route(request, clientIp);
            response.write(out);

            long latencyMillis = (System.nanoTime() - startNanos) / 1_000_000;
            metricsCollector.recordRequest(latencyMillis, response.getStatusCode());

            System.out.printf("%s %s %s -> %d (%dms)%n",
                    clientIp,
                    request.getMethod(),
                    request.getPath(),
                    response.getStatusCode(),
                    latencyMillis);

            requestLogger.log(clientIp,
                    request.getMethod(),
                    request.getPath(),
                    response.getStatusCode(),
                    latencyMillis);

        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
            metricsCollector.recordRequest((System.nanoTime() - startNanos) / 1_000_000, 500);
        } finally {
            metricsCollector.connectionClosed();
        }
    }

    private HttpResponse route(HttpRequest request, String clientIp) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return HttpResponse.badRequest("Only GET is supported in Phase 1");
        }
        if (!router.isMonitoringEndpoint(request.getPath()) && !rateLimiter.allow(clientIp)) {
            return HttpResponse.tooManyRequests();
        }
        return router.route(request.getPath());
    }
}