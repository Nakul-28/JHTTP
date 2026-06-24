package com.httpserver.server;

import com.httpserver.cache.CacheManager;
import com.httpserver.http.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileHandlerTest {

    private static FileHandler fileHandler;
    private static CacheManager cacheManager;

    @BeforeAll
    static void setUp() {
        cacheManager = new CacheManager();
        fileHandler = new FileHandler("www", cacheManager);
    }

    @Test
    void servesExistingFileWith200() {
        HttpResponse response = fileHandler.serve("/index.html");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void rootPathMapsToIndexHtml() {
        HttpResponse response = fileHandler.serve("/");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void missingFileReturns404() {
        HttpResponse response = fileHandler.serve("/does-not-exist.html");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void directoryTraversalWithDotDotIsBlocked() {
        HttpResponse response = fileHandler.serve("/../pom.xml");
        assertEquals(403, response.getStatusCode());
    }

    @Test
    void encodedTraversalAttemptIsBlockedOrNotFound() {
        HttpResponse response = fileHandler.serve("/../../../../../../etc/passwd");
        assertEquals(403, response.getStatusCode());
    }

    @Test
    void queryStringIsStrippedBeforeResolving() {
        HttpResponse response = fileHandler.serve("/index.html?foo=bar");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void successfulResponseIsStoredInCache() {
        fileHandler.serve("/about.html");
        assertNotNull(cacheManager.get("/about.html"));
    }

    @Test
    void cachedEntryIsReusedForEquivalentRequests() {
        fileHandler.serve("/index.html?cache=true");
        assertNotNull(cacheManager.get("/index.html"));
        assertEquals(2, cacheManager.size());
    }
}