package com.miempresa.redis.application.service;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.application.port.out.RateLimitPersistencePort;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;
import com.miempresa.redis.domain.service.UrlNormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación reactivo que implementa el caso de uso de rate
 * limiting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService implements RateLimitUseCase {

  private final RateLimitPersistencePort persistencePort;
  private final UrlNormalizationService urlNormalizationService;

  @Override
  public Mono<Boolean> isRequestAllowed(RequestInfo requestInfo) {
    // Normalizar el endpoint
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(requestInfo.getEndpoint());
    RequestInfo normalizedRequestInfo = RequestInfo.builder()
        .endpoint(normalizedEndpoint)
        .clientIp(requestInfo.getClientIp())
        .requestId(requestInfo.getRequestId())
        .build();

    log.debug("Checking rate limit for endpoint: {} and IP: {}", normalizedEndpoint, requestInfo.getClientIp());

    // Obtener configuración desde el puerto de persistencia de forma reactiva
    return persistencePort.getConfiguration(normalizedEndpoint)
        .flatMap(config -> {
          if (!config.isRateLimitEnabled()) {
            log.debug("No rate limiting applied for endpoint: {} - returning true", normalizedEndpoint);
            return Mono.just(true);
          }

          // Obtener contador actual de forma reactiva
          return persistencePort.getCurrentRequestCount(normalizedRequestInfo)
              .flatMap(currentCount -> {
                log.debug("Current count for endpoint {} and IP {}: {}", normalizedEndpoint, requestInfo.getClientIp(),
                    currentCount);

                if (config.hasReachedLimit(currentCount)) {
                  log.warn("Rate limit exceeded for endpoint: {} and IP: {} ({} >= {})",
                      normalizedEndpoint, requestInfo.getClientIp(), currentCount, config.getMaxRequests());
                  return Mono.just(false);
                }

                // Incrementar contador de forma reactiva
                return persistencePort.incrementRequestCount(normalizedRequestInfo, config.getTimeWindowSeconds())
                    .then(Mono.just(true))
                    .doOnSuccess(result -> log.debug("Request allowed for endpoint: {} and IP: {} - count incremented",
                        normalizedEndpoint, requestInfo.getClientIp()));
              });
        })
        .defaultIfEmpty(true) // Si no hay configuración, permitir el request
        .doOnError(error -> log.error("Error during rate limiting for endpoint: {} and IP: {}",
            normalizedEndpoint, requestInfo.getClientIp(), error));
  }

  @Override
  public Mono<Void> updateConfiguration(String endpoint, int maxRequests, int timeWindowSeconds, boolean enabled) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(endpoint);

    log.info("Updating rate limit configuration for endpoint: {} - maxRequests: {}, timeWindow: {}s, enabled: {}",
        normalizedEndpoint, maxRequests, timeWindowSeconds, enabled);

    RateLimitConfig config = RateLimitConfig.builder()
        .endpoint(normalizedEndpoint)
        .maxRequests(maxRequests)
        .timeWindowSeconds(timeWindowSeconds)
        .enabled(enabled)
        .build();

    return persistencePort.saveConfiguration(config)
        .then(Mono.defer(() -> {
          if (!enabled) {
            // Limpiar datos existentes si se deshabilita
            log.info("Clearing rate limit data for disabled endpoint: {}", normalizedEndpoint);
            return persistencePort.clearRateLimitData(normalizedEndpoint);
          }
          return Mono.empty();
        }))
        .doOnSuccess(
            result -> log.info("Rate limit configuration updated successfully for endpoint: {}", normalizedEndpoint))
        .doOnError(
            error -> log.error("Error updating rate limit configuration for endpoint: {}", normalizedEndpoint, error));
  }

  @Override
  public Mono<RateLimitConfig> getConfiguration(String endpoint) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(endpoint);
    log.debug("Getting rate limit configuration for endpoint: {}", normalizedEndpoint);

    return persistencePort.getConfiguration(normalizedEndpoint)
        .doOnSuccess(config -> {
          if (config != null) {
            log.debug("Configuration found for endpoint: {} - {}", normalizedEndpoint, config);
          } else {
            log.debug("No configuration found for endpoint: {}", normalizedEndpoint);
          }
        })
        .doOnError(
            error -> log.error("Error getting rate limit configuration for endpoint: {}", normalizedEndpoint, error));
  }
}