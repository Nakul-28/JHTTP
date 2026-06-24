package com.httpserver.server;

/**
 * Worker thread for the custom Phase 5 thread pool.
 */
public class WorkerThread extends Thread {

    private final ThreadPool threadPool;

    public WorkerThread(ThreadPool threadPool, String name) {
        super(name);
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Runnable task = threadPool.take();
                if (task == null) {
                    return;
                }
                task.run();
            } catch (InterruptedException e) {
                interrupt();
                return;
            } catch (RuntimeException e) {
                System.err.println("Worker " + getName() + " failed: " + e.getMessage());
            }
        }
    }
}