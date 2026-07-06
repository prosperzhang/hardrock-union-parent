package com.hardrockunion.boot.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "系统健康检查")
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider;

    public HealthCheckController(
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
        ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider
    ) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.redisConnectionFactoryProvider = redisConnectionFactoryProvider;
    }

    @Operation(summary = "健康检查")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "hardrock-union");
        response.put("status", "UP");
        response.put("database", databaseStatus());
        response.put("redis", redisStatus());
        return Result.success(response);
    }

    private Map<String, Object> databaseStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            status.put("status", "NOT_CONFIGURED");
            return status;
        }

        try {
            Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("status", 1 == (value == null ? 0 : value) ? "UP" : "UNKNOWN");
        } catch (Exception ex) {
            status.put("status", "DOWN");
            status.put("message", ex.getMessage());
        }
        return status;
    }

    private Map<String, Object> redisStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        RedisConnectionFactory factory = redisConnectionFactoryProvider.getIfAvailable();
        if (factory == null) {
            status.put("status", "NOT_CONFIGURED");
            return status;
        }

        try (var connection = factory.getConnection()) {
            String pong = connection.ping();
            status.put("status", "PONG".equalsIgnoreCase(pong) ? "UP" : "UNKNOWN");
        } catch (Exception ex) {
            status.put("status", "DOWN");
            status.put("message", ex.getMessage());
        }
        return status;
    }
}
