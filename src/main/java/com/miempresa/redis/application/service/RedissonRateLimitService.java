package com.miempresa.redis.application.service;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.application.port.out.DistributedCachePort;
import com.miempresa.redis.application.port.out.DistributedLockPort;
import com.miempresa.redis.application.port.out.RateLimitPersistencePort;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;
import com.miempresa.redis.domain.service.UrlNormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * Servicio de rate limiting mejorado usando funcionalidades distribuidas
 * Implementa buenas prácticas para programación reactiva y control de
 * concurrencia
 * Sigue arquitectura hexagonal usando puertos de salida
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonRateLimitService implements RateLimitUseCase {

  private final RateLimitPersistencePort persistencePort;
  private final UrlNormalizationService urlNormalizationService;
  private final DistributedLockPort distributedLockPort;
  private final DistributedCachePort distributedCachePort;

  @Override
  public Mono<Boolean> isRequestAllowed(RequestInfo requestInfo) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(requestInfo.getEndpoint());
    RequestInfo normalizedRequestInfo = RequestInfo.builder()
        .endpoint(normalizedEndpoint)
        .clientIp(requestInfo.getClientIp())
        .requestId(requestInfo.getRequestId())
        .build();

    log.debug("Checking rate limit for endpoint: {} and IP: {}", normalizedEndpoint, requestInfo.getClientIp());

    return getConfigurationWithCache(normalizedEndpoint)
        .flatMap(config -> {
          if (!config.isRateLimitEnabled()) {
            log.debug("No rate limiting applied for endpoint: {} - returning true", normalizedEndpoint);
            return Mono.just(true);
          }

          return processRateLimitRequest(normalizedRequestInfo, config);
        })
        .defaultIfEmpty(true) // Si no hay configuración, permitir el request
        .doOnError(error -> log.error("Error during rate limiting for endpoint: {} and IP: {}",
            normalizedEndpoint, requestInfo.getClientIp(), error))
        .onErrorReturn(true); // En caso de error, permitir el request (fail-safe)
  }

  @Override
  public Mono<Void> updateConfiguration(String endpoint, int maxRequests, int timeWindowSeconds, boolean enabled) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(endpoint);

    log.info("Updating rate limit configuration for endpoint: {} - maxRequests: {}, timeWindow: {}s, enabled: {}",
        normalizedEndpoint, maxRequests, timeWindowSeconds, enabled);

    RateLimitConfig newConfig = RateLimitConfig.builder()
        .endpoint(normalizedEndpoint)
        .maxRequests(maxRequests)
        .timeWindowSeconds(timeWindowSeconds)
        .enabled(enabled)
        .build();

    // Usar lock distribuido para evitar condiciones de carrera
    return updateConfigurationWithLock(normalizedEndpoint, newConfig);
  }

  @Override
  public Mono<RateLimitConfig> getConfiguration(String endpoint) {
    String normalizedEndpoint = urlNormalizationService.normalizeEndpoint(endpoint);
    log.debug("Getting rate limit configuration for endpoint: {}", normalizedEndpoint);

    return getConfigurationWithCache(normalizedEndpoint)
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

  /**
   * Procesa el rate limiting con lock distribuido para evitar condiciones de
   * carrera
   */
  private Mono<Boolean> processRateLimitRequest(RequestInfo requestInfo, RateLimitConfig config) {
    String lockKey = "rate-limit:request:" + requestInfo.getRateLimitKey();

    return distributedLockPort.tryLock(lockKey, config.getLockTimeout(), TimeUnit.MILLISECONDS)
        .flatMap(lockAcquired -> {
          if (lockAcquired) {
            return processRateLimitRequestAtomically(requestInfo, config)
                .doFinally(signalType -> distributedLockPort.unlock(lockKey).subscribe());
          } else {
            log.warn("Could not acquire lock for rate limiting request: {} - allowing request as fallback", lockKey);
            return Mono.just(true); // Fallback: permitir request si no se puede obtener lock
          }
        })
        .onErrorReturn(true); // Fallback: permitir request en caso de error
  }

  /**
   * Procesa el rate limiting de forma atómica
   */
  private Mono<Boolean> processRateLimitRequestAtomically(RequestInfo requestInfo, RateLimitConfig config) {
    return persistencePort.getCurrentRequestCount(requestInfo)
        .defaultIfEmpty(0)
        .flatMap(currentCount -> {
          log.debug("Current count for endpoint {} and IP {}: {}",
              requestInfo.getEndpoint(), requestInfo.getClientIp(), currentCount);

          if (config.hasReachedLimit(currentCount)) {
            log.warn("Rate limit exceeded for endpoint: {} and IP: {} ({} >= {})",
                requestInfo.getEndpoint(), requestInfo.getClientIp(), currentCount, config.getMaxRequests());
            return Mono.just(false);
          }

          // Incrementar contador
          return persistencePort.incrementRequestCount(requestInfo, config.getTimeWindowSeconds())
              .then(Mono.just(true))
              .doOnSuccess(result -> log.debug("Request allowed for endpoint: {} and IP: {} - count incremented",
                  requestInfo.getEndpoint(), requestInfo.getClientIp()));
        });
  }

  /**
   * Actualiza configuración con lock distribuido para evitar condiciones de
   * carrera
   */
  private Mono<Void> updateConfigurationWithLock(String endpoint, RateLimitConfig newConfig) {
    String lockKey = "rate-limit:config:lock:" + endpoint;

    return distributedLockPort.tryLock(lockKey, newConfig.getLockTimeout(), TimeUnit.MILLISECONDS)
        .flatMap(lockAcquired -> {
          if (lockAcquired) {
            return updateConfigurationAtomically(endpoint, newConfig)
                .doFinally(signalType -> distributedLockPort.unlock(lockKey).subscribe());
          } else {
            return Mono.error(new RuntimeException("Could not acquire lock for configuration update: " + endpoint));
          }
        });
  }

  /**
   * Actualiza la configuración de forma atómica
   */
  private Mono<Void> updateConfigurationAtomically(String endpoint, RateLimitConfig newConfig) {
    return persistencePort.saveConfiguration(newConfig)
        .then(updateConfigurationCache(endpoint, newConfig))
        .then(Mono.defer(() -> {
          // Limpiar datos si se deshabilita
          if (!newConfig.isEnabled()) {
            log.info("Clearing rate limit data for disabled endpoint: {}", endpoint);
            return persistencePort.clearRateLimitData(endpoint);
          }
          return Mono.empty();
        }))
        .doOnSuccess(result -> log.info("Rate limit configuration updated successfully for endpoint: {}", endpoint));
  }

  /**
   * Obtiene configuración del cache distribuido o de la persistencia
   */
  private Mono<RateLimitConfig> getConfigurationWithCache(String endpoint) {
    return distributedCachePort.get(endpoint, RateLimitConfig.class)
        .switchIfEmpty(Mono.defer(() -> {
          // Si no está en cache, obtener de persistencia
          return persistencePort.getConfiguration(endpoint)
              .flatMap(persistedConfig -> {
                if (persistedConfig != null) {
                  // Guardar en cache distribuido
                  return distributedCachePort.put(endpoint, persistedConfig)
                      .thenReturn(persistedConfig);
                }
                return Mono.empty();
              });
        }));
  }

  /**
   * Actualiza el cache distribuido con nueva configuración
   */
  private Mono<Void> updateConfigurationCache(String endpoint, RateLimitConfig config) {
    return distributedCachePort.put(endpoint, config)
        .doOnSuccess(result -> log.debug("Configuration cache updated for endpoint: {}", endpoint))
        .onErrorResume(error -> {
          log.warn("Failed to update configuration cache for endpoint: {}", endpoint, error);
          return Mono.empty(); // No fallar la operación principal por error en cache
        });
  }
}