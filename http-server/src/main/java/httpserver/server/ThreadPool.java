package com.httpserver.server;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Phase 5/11: a small custom thread pool built with worker threads and a
 * blocking task queue, with graceful shutdown support.
 */
public class ThreadPool {

    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private final WorkerThread[] workers;
    private boolean running = true;

    public ThreadPool(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Thread pool size must be greater than zero");
        }

        workers = new WorkerThread[size];
        for (int index = 0; index < size; index++) {
            workers[index] = new WorkerThread(this, "http-worker-" + index);
            workers[index].start();
        }
    }

    public void submit(Runnable task) {
        synchronized (taskQueue) {
            if (!running) {
                throw new IllegalStateException("Thread pool is shut down");
            }
            taskQueue.offer(task);
            taskQueue.notify();
        }
    }

    Runnable take() throws InterruptedException {
        synchronized (taskQueue) {
            while (taskQueue.isEmpty() && running) {
                taskQueue.wait();
            }

            if (taskQueue.isEmpty()) {
                return null;
            }

            return taskQueue.poll();
        }
    }

    public void shutdown() {
        synchronized (taskQueue) {
            running = false;
            taskQueue.notifyAll();
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        for (WorkerThread worker : workers) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                return false;
            }
            worker.join(TimeUnit.NANOSECONDS.toMillis(remainingNanos));
            if (worker.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRunning() {
        synchronized (taskQueue) {
            return running;
        }
    }
}