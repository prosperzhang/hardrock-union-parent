package com.hardrockunion.boot.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;

@RestController
@RequestMapping("/api/health/virtual-threads")
public class VirtualThreadController {

    private final VirtualThreadProbeService virtualThreadProbeService;

    public VirtualThreadController(VirtualThreadProbeService virtualThreadProbeService) {
        this.virtualThreadProbeService = virtualThreadProbeService;
    }

    @GetMapping("/probe")
    public Result<Map<String, Object>> probe(
        @RequestParam(value = "tasks", defaultValue = "8") int tasks,
        @RequestParam(value = "sleepMs", defaultValue = "200") long sleepMs
    ) {
        int safeTasks = Math.max(1, Math.min(tasks, 128));
        long safeSleepMs = Math.max(10L, Math.min(sleepMs, 5_000L));
        Instant startedAt = Instant.now();

        List<CompletableFuture<Map<String, Object>>> futures = java.util.stream.IntStream.range(0, safeTasks)
            .mapToObj(index -> virtualThreadProbeService.probe(index, safeSleepMs))
            .toList();

        List<Map<String, Object>> results = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tasks", safeTasks);
        response.put("sleepMs", safeSleepMs);
        response.put("elapsedMs", Duration.between(startedAt, Instant.now()).toMillis());
        response.put("allVirtual", results.stream().allMatch(item -> Boolean.TRUE.equals(item.get("virtual"))));
        response.put("results", results);
        return Result.success(response);
    }

    @GetMapping("/ping")
    public Result<String> ping(
        @RequestParam(value = "tasks", defaultValue = "1") int tasks,
        @RequestParam(value = "sleepMs", defaultValue = "10") long sleepMs
    ) {
        int safeTasks = Math.max(1, Math.min(tasks, 256));
        long safeSleepMs = Math.max(0L, Math.min(sleepMs, 1_000L));

        List<CompletableFuture<Void>> futures = java.util.stream.IntStream.range(0, safeTasks)
            .mapToObj(index -> virtualThreadProbeService.sleep(safeSleepMs))
            .toList();
        futures.forEach(CompletableFuture::join);
        return Result.success("OK");
    }
}
