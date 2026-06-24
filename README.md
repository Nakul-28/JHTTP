# JHTTP

> A multithreaded HTTP/1.1 server built from scratch in Java using raw TCP sockets, featuring a custom thread pool, response caching, request logging, IP-based rate limiting, REST API routing, and real-time server metrics.

## Overview

JHTTP is a production-inspired HTTP server developed to understand the internals of modern web servers without relying on frameworks such as Spring Boot or Netty.

Instead of using existing HTTP libraries, JHTTP implements the core building blocks manually, including socket communication, HTTP request parsing, concurrent request processing, static file serving, caching, logging, and performance optimization.

The project demonstrates concepts from:

* Computer Networks
* Operating Systems
* Concurrent Programming
* Synchronization
* Backend Systems Design
* Performance Engineering

---

## Features

### HTTP Server

* Raw TCP socket communication using `ServerSocket`
* HTTP/1.1 request parsing
* Static file serving
* MIME type detection
* Custom HTTP response generation
* Error handling (400, 403, 404, 429, 500)

---

### Concurrent Request Processing

* Custom producer-consumer thread pool
* Fixed-size worker threads
* Blocking task queue
* Safe concurrent request handling

---

### Response Caching

* In-memory response cache
* Thread-safe implementation using `ConcurrentHashMap`
* Eliminates repeated disk reads for frequently accessed files

---

### Request Logging

Logs every request including:

* Client IP
* HTTP Method
* Requested Path
* Status Code
* Request Latency

---

### IP-based Rate Limiting

Implements a fixed-window rate limiter.

* Default limit: **60 requests/minute/IP**
* Returns **HTTP 429 Too Many Requests**
* Automatically resets after the time window expires

---

### API Routing

Built-in API endpoints:

```
GET /api/time
GET /api/health
GET /api/stats
```

---

### Server Metrics

Tracks runtime statistics including:

* Total Requests
* Cache Hits
* Cache Misses
* Error Responses
* Average Response Latency

---

## Project Structure

```
src
└── main
    └── java
        └── com.httpserver
            ├── api
            ├── cache
            ├── handler
            ├── http
            ├── logging
            ├── metrics
            ├── ratelimit
            ├── server
            └── threadpool

www
├── index.html
├── about.html
└── style.css
```

---

## Architecture

```
                    +----------------------+
                    |     HTTP Client      |
                    +----------+-----------+
                               |
                               |
                        TCP Connection
                               |
                               ▼
                    +----------------------+
                    |    ServerSocket      |
                    +----------+-----------+
                               |
                               ▼
                    +----------------------+
                    |  Custom Thread Pool  |
                    +----------+-----------+
                               |
            +------------------+------------------+
            |                                     |
            ▼                                     ▼
      Rate Limiter                         HTTP Parser
            |                                     |
            +------------------+------------------+
                               |
                               ▼
                          Router
               +---------------+---------------+
               |                               |
               ▼                               ▼
        Static File Handler              API Handler
               |                               |
               ▼                               ▼
          Response Cache                 JSON Response
               |
               ▼
          HTTP Response
```

---

## Performance Optimizations

* Custom thread pool to avoid creating a thread per request
* Thread-safe concurrent request processing
* In-memory caching for static resources
* Efficient request routing
* Fixed-window rate limiting
* Lightweight response generation

---

## Benchmark Results

Benchmarked using **ApacheBench (ab)**.

### Test Configuration

* Total Requests: **100,000**
* Static Resource: `/about.html`
* Response Size: **432 Bytes**

| Concurrent Clients | Requests/sec |   Avg Latency | P95 Latency | Max Latency | Failed Requests |
| -----------------: | -----------: | ------------: | ----------: | ----------: | --------------: |
|                 50 |  **1629.80** |  **30.68 ms** |   **40 ms** |   **83 ms** |           **0** |
|                100 |  **1609.43** |  **62.13 ms** |   **81 ms** |  **140 ms** |           **0** |
|                200 |  **1675.08** | **119.40 ms** |  **140 ms** |  **175 ms** |           **0** |

### Observations

* Successfully handled **100,000 requests** with **0 failed connections**.
* Maintained approximately **1,600 requests/sec** under sustained load.
* Throughput remained stable as concurrency increased.
* Increased concurrency primarily affected latency due to queueing within the fixed-size thread pool.

---

## Technologies Used

* Java 21
* Maven
* Java Sockets
* Java Concurrency API
* ApacheBench (Performance Testing)

---

## Building

```bash
mvn clean package
```

---

## Running

```bash
java -jar target/http-server.jar
```

The server starts on:

```
http://localhost:9000
```

---

## Example API Endpoints

### Health

```
GET /api/health
```

Response

```json
{
    "status": "UP"
}
```

---

### Current Time

```
GET /api/time
```

---

### Server Statistics

```
GET /api/stats
```

Returns runtime metrics including request count, cache statistics, and latency information.

---

## Future Improvements

* HTTP Keep-Alive
* LRU Response Cache
* HTTPS (TLS)
* GZIP Compression
* HTTP/2 Support
* Non-blocking NIO implementation
* Virtual Threads (Project Loom)
* Configurable server properties
* Unit & Integration Tests

---

## Key Learnings

Building JHTTP provided hands-on experience with:

* TCP socket programming
* HTTP protocol internals
* Concurrent programming
* Producer-consumer architecture
* Thread synchronization
* Caching strategies
* Rate limiting algorithms
* Backend performance optimization
* Load testing and benchmarking
* Systems programming fundamentals
