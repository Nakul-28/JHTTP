# Multithreaded HTTP Server in Java

Phase 12 implementation: a multithreaded HTTP server built on raw TCP sockets
using a custom thread pool, request logging, in-memory response caching,
IP-based rate limiting, dynamic API routes, runtime metrics, graceful shutdown,
and a repeatable load-testing workflow.

## Build & Run

This is a standard Maven project (Java 21).

```bash
mvn clean package
java -jar target/http-server.jar
```

Or run directly without packaging:

```bash
mvn compile exec:java -Dexec.mainClass="com.httpserver.server.HttpServer"
```

By default the server listens on **port 8080** and serves static files from `www/`.
It writes request logs to `server.log` in the project root.
It allows up to `60` requests per `60` seconds per client IP.

```bash
curl http://localhost:8080/index.html
curl http://localhost:8080/api/time
curl http://localhost:8080/api/health
curl http://localhost:8080/api/stats
```

## Project Structure

```text
src/main/java/com/httpserver/
|-- server/
|   |-- HttpServer.java        # accept loop with graceful shutdown support
|   |-- ConnectionHandler.java # handles one client socket end-to-end
|   |-- ThreadPool.java        # custom blocking queue + graceful termination waiting
|   |-- WorkerThread.java      # worker threads consuming queued tasks
|   `-- FileHandler.java       # serves static files with in-memory caching
|-- routing/
|   |-- Router.java            # dispatches API requests vs static files
|   `-- ApiHandler.java        # implements /api/time, /api/health, /api/stats
|-- metrics/
|   `-- MetricsCollector.java  # tracks requests, cache stats, latency, errors, active connections
|-- cache/
|   |-- CacheManager.java      # concurrent map of cached file responses
|   `-- CachedFile.java        # immutable cached body + content type
|-- logging/
|   `-- RequestLogger.java     # writes timestamped request logs to server.log
|-- ratelimit/
|   `-- RateLimiter.java       # fixed-window rate limiting per client IP
|-- http/
|   |-- HttpRequest.java       # parses request line + headers
|   `-- HttpResponse.java      # builds and writes HTTP responses
www/
|-- index.html
|-- about.html
`-- style.css
```

## Status: Phase 12 of 12

- [x] Phase 1 - Single-threaded server, static file serving
- [x] Phase 2 - Request parsing
- [x] Phase 3 - Static file server hardening
- [x] Phase 4 - Thread pool (`ExecutorService`)
- [x] Phase 5 - Custom thread pool
- [x] Phase 6 - Request logging
- [x] Phase 7 - Response caching
- [x] Phase 8 - Rate limiting
- [x] Phase 9 - API support
- [x] Phase 10 - Metrics endpoint
- [x] Phase 11 - Graceful shutdown
- [x] Phase 12 - Load testing

## Phase 12 Notes

Load testing is done against the finished server using external tools such as
Apache Bench (`ab`) or `wrk`. During a benchmark, use `/api/stats` and
`server.log` to correlate throughput, cache hit rate, latency, and error counts.

## Load Testing Workflow

1. Start the server:

```bash
java -jar target/http-server.jar
```

2. Warm the cache before benchmarking static files:

```bash
curl http://localhost:8080/index.html
curl http://localhost:8080/about.html
```

3. Run one of the following benchmarks.

### Apache Bench

```bash
ab -n 10000 -c 100 http://127.0.0.1:8080/index.html
ab -n 5000 -c 50 http://127.0.0.1:8080/api/health
```

### wrk

```bash
wrk -t4 -c100 -d30s http://127.0.0.1:8080/index.html
wrk -t2 -c40 -d15s http://127.0.0.1:8080/api/stats
```

4. Inspect runtime metrics while or after the run:

```bash
curl http://localhost:8080/api/stats
```

5. Review the request log for latency and status patterns:

```powershell
Get-Content .\server.log | Select-Object -Last 20
```

## What To Record

- Requests per second from `ab` or `wrk`
- Average and percentile latency from the load tool
- `cacheHitRate` from `/api/stats`
- `errorCount` from `/api/stats`
- Whether monitoring endpoints stayed responsive during load

## Benchmark Tips

- Benchmark `127.0.0.1` instead of `localhost` to avoid extra name resolution noise.
- Warm static assets first so Phase 7 cache behavior is visible.
- Use `/api/health` or `/api/stats` for lighter dynamic-route benchmarks.
- If you want to stress rate limiting specifically, use a single client and watch for `429` responses.
- If you want raw throughput numbers, temporarily raise the rate limit or distribute requests across clients.