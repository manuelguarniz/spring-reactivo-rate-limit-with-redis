package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import com.miempresa.redis.infrastructure.health.RedissonHealthIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador reactivo para health checks de la aplicación
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

  private final RedissonHealthIndicator redissonHealthIndicator;

  @GetMapping("/health")
  public Mono<ResponseEntity<Map<String, Object>>> health() {
    Map<String, Object> response = new HashMap<>();

    // Información básica de la aplicación
    response.put("status", "UP");
    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    response.put("message", "Redis API is running successfully");
    response.put("version", "1.0.0");

    // Información de Redisson
    boolean redissonHealthy = redissonHealthIndicator.isHealthy();
    response.put("redisson", redissonHealthy ? "UP" : "DOWN");
    response.put("redissonInfo", redissonHealthIndicator.getHealthInfo());

    // Estado general
    if (!redissonHealthy) {
      response.put("status", "DEGRADED");
      response.put("message", "Redis API is running but Redisson has issues");
    }

    log.debug("Health check completed - Redisson: {}", redissonHealthy ? "UP" : "DOWN");

    return Mono.just(ResponseEntity.ok(response));
  }
}