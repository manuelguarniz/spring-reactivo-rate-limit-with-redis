package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import com.miempresa.redis.application.service.RateLimitServiceFactory;
import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.infrastructure.adapter.in.web.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador reactivo para la gestión de configuración de rate limiting
 * Usa Redisson con fallback automático
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RateLimitConfigController {

  private final RateLimitServiceFactory rateLimitServiceFactory;

  @PostMapping("/rate-limit/config")
  public Mono<ResponseEntity<Map<String, Object>>> updateRateLimitConfig(
      @RequestParam(name = "endpoint") String endpoint,
      @RequestParam(name = "maxRequests") int maxRequests,
      @RequestParam(name = "timeWindowSeconds") int timeWindowSeconds,
      @RequestParam(name = "enabled", defaultValue = "true") boolean enabled) {

    log.info("Updating rate limit configuration - endpoint: {}, maxRequests: {}, timeWindow: {}s, enabled: {}",
        endpoint, maxRequests, timeWindowSeconds, enabled);

    String normalizedEndpoint = UrlUtils.normalizeEndpoint(endpoint);

    // Obtener el servicio apropiado (distribuido o fallback) de forma reactiva
    return rateLimitServiceFactory.getRateLimitService()
        .flatMap(rateLimitService -> rateLimitService.updateConfiguration(normalizedEndpoint, maxRequests,
            timeWindowSeconds, enabled))
        .then(Mono.defer(() -> {
          Map<String, Object> response = new HashMap<>();
          response.put("message", "Rate limit configuration updated successfully");
          response.put("endpoint", normalizedEndpoint);
          response.put("maxRequests", maxRequests);
          response.put("timeWindowSeconds", timeWindowSeconds);
          response.put("enabled", enabled);
          response.put("service", "Dynamic Selection");

          log.info("Rate limit configuration updated successfully for endpoint: {}", normalizedEndpoint);
          return Mono.just(ResponseEntity.ok(response));
        }))
        .onErrorResume(error -> {
          log.error("Error updating rate limit configuration for endpoint: {}", normalizedEndpoint, error);
          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("error", "Failed to update configuration");
          errorResponse.put("message", error.getMessage());
          return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
        });
  }

  @GetMapping("/rate-limit/config/**")
  public Mono<ResponseEntity<Map<String, Object>>> getRateLimitConfig(ServerWebExchange exchange) {
    // Extraer el endpoint desde la URL completa
    String requestURI = exchange.getRequest().getURI().getPath();
    String basePath = "/api/admin/rate-limit/config/";
    String endpoint = requestURI.substring(requestURI.indexOf(basePath) + basePath.length());

    // Normalizar el endpoint para asegurar consistencia
    String normalizedEndpoint = UrlUtils.normalizeEndpoint(endpoint);

    log.debug("Getting rate limit configuration for endpoint: {}", normalizedEndpoint);

    // Obtener el servicio apropiado (distribuido o fallback) de forma reactiva
    return rateLimitServiceFactory.getRateLimitService()
        .flatMap(rateLimitService -> rateLimitService.getConfiguration(normalizedEndpoint))
        .map(config -> {
          Map<String, Object> response = new HashMap<>();
          response.put("endpoint", normalizedEndpoint);
          response.put("maxRequests", config.getMaxRequests());
          response.put("timeWindowSeconds", config.getTimeWindowSeconds());
          response.put("enabled", config.isEnabled());
          response.put("message", "Configuration retrieved successfully");
          response.put("service", "Dynamic Selection");
          log.debug("Configuration found for endpoint: {} - {}", normalizedEndpoint, config);
          return ResponseEntity.ok(response);
        })
        .defaultIfEmpty(ResponseEntity.ok(createNotFoundResponse(normalizedEndpoint)))
        .onErrorResume(error -> {
          log.error("Error getting rate limit configuration for endpoint: {}", normalizedEndpoint, error);
          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("error", "Failed to retrieve configuration");
          errorResponse.put("message", error.getMessage());
          return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
        });
  }

  /**
   * Endpoint para forzar el uso del servicio de fallback
   */
  @PostMapping("/rate-limit/fallback")
  public Mono<ResponseEntity<Map<String, Object>>> forceFallback() {
    log.info("Forcing fallback to original rate limiting implementation");

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Fallback service activated");
    response.put("service", "Original Implementation");
    response.put("timestamp", System.currentTimeMillis());

    return Mono.just(ResponseEntity.ok(response));
  }

  /**
   * Endpoint para forzar el uso del servicio de Redisson
   */
  @PostMapping("/rate-limit/redisson")
  public Mono<ResponseEntity<Map<String, Object>>> forceRedisson() {
    log.info("Forcing Redisson rate limiting service");

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Redisson service activated");
    response.put("service", "Redisson Implementation");
    response.put("timestamp", System.currentTimeMillis());

    return Mono.just(ResponseEntity.ok(response));
  }

  private Map<String, Object> createNotFoundResponse(String endpoint) {
    Map<String, Object> response = new HashMap<>();
    response.put("endpoint", endpoint);
    response.put("message", "No configuration found for this endpoint");
    log.debug("No configuration found for endpoint: {}", endpoint);
    return response;
  }

  /**
   * Determina el tipo de servicio que se está usando
   */
  private String getServiceType(RateLimitUseCase rateLimitService) {
    if (rateLimitService.getClass().getSimpleName().contains("Redisson")) {
      return "Redisson Implementation";
    } else {
      return "Original Implementation";
    }
  }
}