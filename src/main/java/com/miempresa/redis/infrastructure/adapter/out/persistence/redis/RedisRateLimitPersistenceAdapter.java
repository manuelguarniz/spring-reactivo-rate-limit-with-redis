package com.miempresa.redis.infrastructure.adapter.out.persistence.redis;

import com.miempresa.redis.application.port.out.RateLimitPersistencePort;
import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Adaptador de persistencia Redis para el rate limiting
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimitPersistenceAdapter implements RateLimitPersistencePort {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public int getCurrentRequestCount(RequestInfo requestInfo) {
    String key = requestInfo.getRateLimitKey();
    log.debug("Getting current count from Redis key: {}", key);

    String currentCountStr = (String) redisTemplate.opsForValue().get(key);
    int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;

    log.debug("Current count from Redis: {}", currentCount);
    return currentCount;
  }

  @Override
  public void incrementRequestCount(RequestInfo requestInfo, int timeWindowSeconds) {
    String key = requestInfo.getRateLimitKey();
    log.debug("Incrementing request count for Redis key: {}", key);

    // Obtener contador actual
    String currentCountStr = (String) redisTemplate.opsForValue().get(key);
    int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;

    if (currentCount == 0) {
      // Primera request, establecer con expiración
      redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(timeWindowSeconds));
      log.debug("First request - set count to 1 with TTL: {}s", timeWindowSeconds);
    } else {
      // Incrementar contador existente
      redisTemplate.opsForValue().increment(key);
      log.debug("Incremented count to: {}", currentCount + 1);
    }
  }

  @Override
  public RateLimitConfig getConfiguration(String endpoint) {
    String configKey = "rate-limit:config:" + endpoint;
    log.debug("Getting configuration from Redis key: {}", configKey);

    // Verificar si existe la configuración en Redis
    if (!redisTemplate.hasKey(configKey)) {
      log.debug("No configuration found in Redis for key: {}", configKey);
      return null;
    }

    // Leer configuración desde Redis
    Object maxRequestsObj = redisTemplate.opsForHash().get(configKey, "maxRequests");
    Object timeWindowSecondsObj = redisTemplate.opsForHash().get(configKey, "timeWindowSeconds");
    Object enabledObj = redisTemplate.opsForHash().get(configKey, "enabled");

    // Verificar que todos los campos estén presentes
    if (maxRequestsObj == null || timeWindowSecondsObj == null || enabledObj == null) {
      log.warn("Incomplete configuration in Redis for key: {}", configKey);
      return null;
    }

    // Crear objeto de configuración desde Redis
    RateLimitConfig config = RateLimitConfig.builder()
        .endpoint(endpoint)
        .maxRequests(Integer.parseInt((String) maxRequestsObj))
        .timeWindowSeconds(Integer.parseInt((String) timeWindowSecondsObj))
        .enabled(Boolean.parseBoolean((String) enabledObj))
        .build();

    log.debug("Configuration retrieved from Redis: {}", config);
    return config;
  }

  @Override
  public void saveConfiguration(RateLimitConfig config) {
    String configKey = "rate-limit:config:" + config.getEndpoint();
    log.debug("Saving configuration to Redis key: {}", configKey);

    // Guardar configuración en Redis usando Hash
    redisTemplate.opsForHash().put(configKey, "maxRequests", String.valueOf(config.getMaxRequests()));
    redisTemplate.opsForHash().put(configKey, "timeWindowSeconds", String.valueOf(config.getTimeWindowSeconds()));
    redisTemplate.opsForHash().put(configKey, "enabled", String.valueOf(config.isEnabled()));

    // Reset TTL for the config key
    redisTemplate.persist(configKey);

    log.debug("Configuration saved to Redis successfully");
  }

  @Override
  public void clearRateLimitData(String endpoint) {
    log.debug("Clearing rate limit data for endpoint: {}", endpoint);

    // Patrón para buscar todas las claves de rate limiting para este endpoint
    String pattern = "rate-limit:" + endpoint + ":*";
    Set<String> keysToDelete = redisTemplate.keys(pattern);

    if (keysToDelete != null && !keysToDelete.isEmpty()) {
      redisTemplate.delete(keysToDelete);
      log.debug("Deleted {} rate limit keys for endpoint: {}", keysToDelete.size(), endpoint);
    } else {
      log.debug("No rate limit keys found to delete for endpoint: {}", endpoint);
    }
  }
}