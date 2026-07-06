package com.hardrockunion.boot.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VirtualThreadProbeService {

    @Async
    public CompletableFuture<Map<String, Object>> probe(int index, long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        Thread thread = Thread.currentThread();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskIndex", index);
        result.put("threadName", thread.getName());
        result.put("virtual", thread.isVirtual());
        result.put("threadClass", thread.getClass().getName());
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Void> sleep(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture(null);
    }
}
