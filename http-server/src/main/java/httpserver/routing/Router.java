package com.httpserver.routing;

import com.httpserver.http.HttpResponse;
import com.httpserver.server.FileHandler;

import java.util.Set;

/**
 * Phase 9/10: routes requests between API endpoints and static file serving.
 */
public class Router {

    private static final Set<String> MONITORING_ENDPOINTS = Set.of(
            "/api/time",
            "/api/health",
            "/api/stats");

    private final FileHandler fileHandler;
    private final ApiHandler apiHandler;

    public Router(FileHandler fileHandler, ApiHandler apiHandler) {
        this.fileHandler = fileHandler;
        this.apiHandler = apiHandler;
    }

    public HttpResponse route(String path) {
        String normalizedPath = stripQuery(path);
        if (normalizedPath.startsWith("/api/")) {
            return apiHandler.handle(normalizedPath);
        }
        return fileHandler.serve(path);
    }

    public boolean isMonitoringEndpoint(String path) {
        return MONITORING_ENDPOINTS.contains(stripQuery(path));
    }

    private String stripQuery(String path) {
        int queryIndex = path.indexOf('?');
        if (queryIndex == -1) {
            return path;
        }
        return path.substring(0, queryIndex);
    }
}