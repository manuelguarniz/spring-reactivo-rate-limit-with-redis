package com.miempresa.redis.application.service;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.application.port.out.RateLimitPersistencePort;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;
import com.miempresa.redis.domain.service.UrlNormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación que implementa el caso de uso de rate limiting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService implements RateLimitUseCase {

  private final RateLimitPersistencePort persistencePort;
  private final UrlNormalizationService urlNormalizationService;

  @Override
  public boolean isRequestAllowed(RequestInfo requestInfo) {
    // Normalizar el endpoint
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(requestInfo.getEndpoint());
    RequestInfo normalizedRequestInfo = RequestInfo.builder()
        .endpoint(normalizedEndpoint)
        .clientIp(requestInfo.getClientIp())
        .requestId(requestInfo.getRequestId())
        .build();

    log.debug("Checking rate limit for endpoint: {} and IP: {}", normalizedEndpoint, requestInfo.getClientIp());

    // Obtener configuración desde el puerto de persistencia
    RateLimitConfig config = persistencePort.getConfiguration(normalizedEndpoint);

    if (config == null || !config.isRateLimitEnabled()) {
      log.debug("No rate limiting applied for endpoint: {} - returning true", normalizedEndpoint);
      return true;
    }

    // Obtener contador actual
    int currentCount = persistencePort.getCurrentRequestCount(normalizedRequestInfo);
    log.debug("Current count for endpoint {} and IP {}: {}", normalizedEndpoint, requestInfo.getClientIp(),
        currentCount);

    if (config.hasReachedLimit(currentCount)) {
      log.warn("Rate limit exceeded for endpoint: {} and IP: {} ({} >= {})",
          normalizedEndpoint, requestInfo.getClientIp(), currentCount, config.getMaxRequests());
      return false;
    }

    // Incrementar contador
    persistencePort.incrementRequestCount(normalizedRequestInfo, config.getTimeWindowSeconds());
    log.debug("Request allowed for endpoint: {} and IP: {} - count incremented", normalizedEndpoint,
        requestInfo.getClientIp());

    return true;
  }

  @Override
  public void updateConfiguration(String endpoint, int maxRequests, int timeWindowSeconds, boolean enabled) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(endpoint);

    log.info("Updating rate limit configuration for endpoint: {} - maxRequests: {}, timeWindow: {}s, enabled: {}",
        normalizedEndpoint, maxRequests, timeWindowSeconds, enabled);

    RateLimitConfig config = RateLimitConfig.builder()
        .endpoint(normalizedEndpoint)
        .maxRequests(maxRequests)
        .timeWindowSeconds(timeWindowSeconds)
        .enabled(enabled)
        .build();

    persistencePort.saveConfiguration(config);

    if (!enabled) {
      // Limpiar datos existentes si se deshabilita
      persistencePort.clearRateLimitData(normalizedEndpoint);
      log.info("Cleared rate limit data for disabled endpoint: {}", normalizedEndpoint);
    }
  }

  @Override
  public RateLimitConfig getConfiguration(String endpoint) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(endpoint);
    log.debug("Getting rate limit configuration for endpoint: {}", normalizedEndpoint);
    return persistencePort.getConfiguration(normalizedEndpoint);
  }
}