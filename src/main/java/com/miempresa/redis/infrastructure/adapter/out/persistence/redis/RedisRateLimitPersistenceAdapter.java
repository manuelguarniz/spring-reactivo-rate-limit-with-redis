package com.miempresa.redis.infrastructure.adapter.out.persistence.redis;

import com.miempresa.redis.application.port.out.RateLimitPersistencePort;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;

/**
 * Adaptador de persistencia Redis para el rate limiting (usando RedisTemplate
 * con interfaz reactiva)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimitPersistenceAdapter implements RateLimitPersistencePort {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Mono<Integer> getCurrentRequestCount(RequestInfo requestInfo) {
    String key = requestInfo.getRateLimitKey();
    log.debug("Getting current count from Redis key: {}", key);

    try {
      String currentCountStr = (String) redisTemplate.opsForValue().get(key);
      int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
      log.debug("Current count from Redis: {}", currentCount);
      return Mono.just(currentCount);
    } catch (Exception e) {
      log.error("Error getting current count from Redis key: {}", key, e);
      return Mono.error(e);
    }
  }

  @Override
  public Mono<Void> incrementRequestCount(RequestInfo requestInfo, int timeWindowSeconds) {
    String key = requestInfo.getRateLimitKey();
    log.debug("Incrementing request count for Redis key: {}", key);

    try {
      String currentCountStr = (String) redisTemplate.opsForValue().get(key);

      if (currentCountStr == null) {
        // Primera request, establecer con expiración
        log.debug("First request - set count to 1 with TTL: {}s", timeWindowSeconds);
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(timeWindowSeconds));
      } else {
        // Incrementar contador existente
        redisTemplate.opsForValue().increment(key);
        int currentCount = Integer.parseInt(currentCountStr);
        log.debug("Incremented count to: {}", currentCount + 1);
      }

      return Mono.empty();
    } catch (Exception e) {
      log.error("Error incrementing request count for Redis key: {}", key, e);
      return Mono.error(e);
    }
  }

  @Override
  public Mono<RateLimitConfig> getConfiguration(String endpoint) {
    String configKey = "rate-limit:config:" + endpoint;
    log.debug("Getting configuration from Redis key: {}", configKey);

    try {
      // Verificar si existe la configuración en Redis
      if (!redisTemplate.hasKey(configKey)) {
        log.debug("No configuration found in Redis for key: {}", configKey);
        return Mono.empty();
      }

      // Leer configuración desde Redis
      Object maxRequestsObj = redisTemplate.opsForHash().get(configKey, "maxRequests");
      Object timeWindowSecondsObj = redisTemplate.opsForHash().get(configKey, "timeWindowSeconds");
      Object enabledObj = redisTemplate.opsForHash().get(configKey, "enabled");

      // Verificar que todos los campos estén presentes
      if (maxRequestsObj == null || timeWindowSecondsObj == null || enabledObj == null) {
        log.warn("Incomplete configuration in Redis for key: {}", configKey);
        return Mono.empty();
      }

      // Crear objeto de configuración desde Redis
      RateLimitConfig config = RateLimitConfig.builder()
          .endpoint(endpoint)
          .maxRequests(Integer.parseInt((String) maxRequestsObj))
          .timeWindowSeconds(Integer.parseInt((String) timeWindowSecondsObj))
          .enabled(Boolean.parseBoolean((String) enabledObj))
          .build();

      log.debug("Configuration retrieved from Redis: {}", config);
      return Mono.just(config);
    } catch (Exception e) {
      log.error("Error getting configuration from Redis key: {}", configKey, e);
      return Mono.error(e);
    }
  }

  @Override
  public Mono<Void> saveConfiguration(RateLimitConfig config) {
    String configKey = "rate-limit:config:" + config.getEndpoint();
    log.debug("Saving configuration to Redis key: {}", configKey);

    try {
      // Guardar configuración en Redis usando Hash
      redisTemplate.opsForHash().put(configKey, "maxRequests", String.valueOf(config.getMaxRequests()));
      redisTemplate.opsForHash().put(configKey, "timeWindowSeconds", String.valueOf(config.getTimeWindowSeconds()));
      redisTemplate.opsForHash().put(configKey, "enabled", String.valueOf(config.isEnabled()));

      // Reset TTL for the config key
      redisTemplate.persist(configKey);

      log.debug("Configuration saved to Redis successfully");
      return Mono.empty();
    } catch (Exception e) {
      log.error("Error saving configuration to Redis key: {}", configKey, e);
      return Mono.error(e);
    }
  }

  @Override
  public Mono<Void> clearRateLimitData(String endpoint) {
    log.debug("Clearing rate limit data for endpoint: {}", endpoint);

    try {
      // Patrón para buscar todas las claves de rate limiting para este endpoint
      String pattern = "rate-limit:" + endpoint + ":*";
      Set<String> keysToDelete = redisTemplate.keys(pattern);

      if (keysToDelete != null && !keysToDelete.isEmpty()) {
        redisTemplate.delete(keysToDelete);
        log.debug("Deleted {} rate limit keys for endpoint: {}", keysToDelete.size(), endpoint);
      } else {
        log.debug("No rate limit keys found to delete for endpoint: {}", endpoint);
      }

      return Mono.empty();
    } catch (Exception e) {
      log.error("Error clearing rate limit data for endpoint: {}", endpoint, e);
      return Mono.error(e);
    }
  }
}