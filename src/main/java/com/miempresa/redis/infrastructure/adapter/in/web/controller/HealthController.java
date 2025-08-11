package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para health checks de la aplicación
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    log.debug("Health check request received");

    Map<String, Object> response = new HashMap<>();

    response.put("status", "UP");
    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    response.put("message", "Redis API is running successfully");
    response.put("version", "1.0.0");

    log.debug("Health check response generated - status: UP");
    return ResponseEntity.ok(response);
  }
}