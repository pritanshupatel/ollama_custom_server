package com.example.ollamaadapter.core;

import com.example.ollamaadapter.exception.ApiException;

import java.util.concurrent.Semaphore;

public class ConcurrencyGuard {

    private final Semaphore semaphore;

    public ConcurrencyGuard(int maxConcurrentRequests) {
        this.semaphore = new Semaphore(maxConcurrentRequests, true);
    }

    public GuardHandle acquire() {
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            RequestContext.setErrorSummary("adapter_overloaded");
            throw new ApiException(503, "The adapter is busy. Try again later.", "adapter_overloaded");
        }
        return new GuardHandle(semaphore);
    }

    public static class GuardHandle implements AutoCloseable {

        private final Semaphore semaphore;
        private boolean released;

        public GuardHandle(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void close() {
            if (!released) {
                semaphore.release();
                released = true;
            }
        }
    }
}
