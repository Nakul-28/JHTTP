package com.httpserver.server;

import com.httpserver.cache.CacheManager;
import com.httpserver.cache.CachedFile;
import com.httpserver.http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves request paths against the static file root (www/) and produces
 * an HttpResponse for them.
 *
 * Phase 3 hardening: guards against directory traversal (e.g. "/../secret.txt")
 * by resolving the request path against the root and verifying the resolved
 * path is still contained within it.
 *
 * Phase 7: caches successful static file reads in memory to avoid repeated
 * disk access for the same path.
 */
public class FileHandler {

    private final Path webRoot;
    private final CacheManager cacheManager;

    public FileHandler(String webRootPath) {
        this(webRootPath, new CacheManager());
    }

    public FileHandler(String webRootPath, CacheManager cacheManager) {
        this.webRoot = Path.of(webRootPath).toAbsolutePath().normalize();
        this.cacheManager = cacheManager;
    }

    public HttpResponse serve(String requestPath) {
        String relativePath = normalizeRequestPath(requestPath);
        Path resolved = webRoot.resolve(relativePath.substring(1)).normalize();

        if (!resolved.startsWith(webRoot)) {
            return HttpResponse.forbidden();
        }

        if (!Files.exists(resolved) || Files.isDirectory(resolved)) {
            return HttpResponse.notFound();
        }

        CachedFile cachedFile = cacheManager.get(relativePath);
        if (cachedFile != null) {
            return HttpResponse.ok(cachedFile.getContent(), cachedFile.getContentType());
        }

        try {
            byte[] content = Files.readAllBytes(resolved);
            String contentType = guessContentType(resolved);
            cacheManager.put(relativePath, new CachedFile(content, contentType));
            return HttpResponse.ok(content, contentType);
        } catch (IOException e) {
            return HttpResponse.internalServerError();
        }
    }

    private String normalizeRequestPath(String requestPath) {
        String relativePath = requestPath.equals("/") ? "/index.html" : requestPath;

        int queryIndex = relativePath.indexOf('?');
        if (queryIndex != -1) {
            relativePath = relativePath.substring(0, queryIndex);
        }

        return relativePath;
    }

    private String guessContentType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".txt")) return "text/plain; charset=utf-8";
        return "application/octet-stream";
    }
}