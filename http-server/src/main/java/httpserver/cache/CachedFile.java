package com.httpserver.cache;

/**
 * Immutable cached representation of a static file response.
 */
public class CachedFile {

    private final byte[] content;
    private final String contentType;

    public CachedFile(byte[] content, String contentType) {
        this.content = content.clone();
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content.clone();
    }

    public String getContentType() {
        return contentType;
    }
}