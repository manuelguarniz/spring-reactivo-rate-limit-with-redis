package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.infrastructure.adapter.in.web.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la gestión de configuración de rate limiting
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RateLimitConfigController {

  private final RateLimitUseCase rateLimitUseCase;

  @PostMapping("/rate-limit/config")
  public ResponseEntity<Map<String, Object>> updateRateLimitConfig(
      @RequestParam(name = "endpoint") String endpoint,
      @RequestParam(name = "maxRequests") int maxRequests,
      @RequestParam(name = "timeWindowSeconds") int timeWindowSeconds,
      @RequestParam(name = "enabled", defaultValue = "true") boolean enabled) {

    log.info("Updating rate limit configuration - endpoint: {}, maxRequests: {}, timeWindow: {}s, enabled: {}",
        endpoint, maxRequests, timeWindowSeconds, enabled);

    String normalizedEndpoint = UrlUtils.normalizeEndpoint(endpoint);

    // Usar el caso de uso para actualizar la configuración
    rateLimitUseCase.updateConfiguration(normalizedEndpoint, maxRequests, timeWindowSeconds, enabled);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Rate limit configuration updated successfully");
    response.put("endpoint", normalizedEndpoint);
    response.put("maxRequests", maxRequests);
    response.put("timeWindowSeconds", timeWindowSeconds);
    response.put("enabled", enabled);

    log.info("Rate limit configuration updated successfully for endpoint: {}", normalizedEndpoint);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/rate-limit/config/**")
  public ResponseEntity<Map<String, Object>> getRateLimitConfig(HttpServletRequest request) {
    // Extraer el endpoint desde la URL completa
    String requestURI = request.getRequestURI();
    String basePath = "/api/admin/rate-limit/config/";
    String endpoint = requestURI.substring(requestURI.indexOf(basePath) + basePath.length());

    // Normalizar el endpoint para asegurar consistencia
    String normalizedEndpoint = UrlUtils.normalizeEndpoint(endpoint);

    log.debug("Getting rate limit configuration for endpoint: {}", normalizedEndpoint);

    // Usar el caso de uso para obtener la configuración
    RateLimitConfig config = rateLimitUseCase.getConfiguration(normalizedEndpoint);

    Map<String, Object> response = new HashMap<>();
    response.put("endpoint", normalizedEndpoint);

    if (config != null) {
      response.put("maxRequests", config.getMaxRequests());
      response.put("timeWindowSeconds", config.getTimeWindowSeconds());
      response.put("enabled", config.isEnabled());
      response.put("message", "Configuration retrieved successfully");
      log.debug("Configuration found for endpoint: {} - {}", normalizedEndpoint, config);
    } else {
      response.put("message", "No configuration found for this endpoint");
      log.debug("No configuration found for endpoint: {}", normalizedEndpoint);
    }

    return ResponseEntity.ok(response);
  }
}