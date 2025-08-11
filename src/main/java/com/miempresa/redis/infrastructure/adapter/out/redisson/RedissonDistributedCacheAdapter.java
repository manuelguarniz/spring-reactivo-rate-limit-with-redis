package com.miempresa.redis.infrastructure.adapter.out.redisson;

import com.miempresa.redis.application.port.out.DistributedCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Adaptador de infraestructura para cache distribuido usando Redisson
 * Implementa el puerto de salida DistributedCachePort
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedCacheAdapter implements DistributedCachePort {

  private final RedissonClient redisson;

  @Override
  public <T> Mono<T> get(String key, Class<T> valueType) {
    log.debug("Getting value from cache: {} with type: {}", key, valueType.getSimpleName());

    return Mono.fromCallable(() -> {
      RMap<String, T> cache = redisson.getMap("distributed-cache");
      T value = cache.get(key);
      if (value != null) {
        log.debug("Cache hit for key: {}", key);
      } else {
        log.debug("Cache miss for key: {}", key);
      }
      return value;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error getting value from cache: {}", key, error))
        .onErrorReturn(null);
  }

  @Override
  public <T> Mono<Void> put(String key, T value) {
    log.debug("Putting value in cache: {} with type: {}", key,
        value != null ? value.getClass().getSimpleName() : "null");

    return Mono.fromCallable(() -> {
      RMap<String, T> cache = redisson.getMap("distributed-cache");
      cache.put(key, value);
      log.debug("Value stored in cache successfully: {}", key);
      return null;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .doOnError(error -> log.error("Error storing value in cache: {}", key, error))
        .onErrorComplete();
  }

  @Override
  public <T> Mono<Void> put(String key, T value, long ttl) {
    log.debug("Putting value in cache: {} with TTL: {} seconds", key, ttl);

    return Mono.fromCallable(() -> {
      RMap<String, T> cache = redisson.getMap("distributed-cache");
      cache.put(key, value);
      // Para RMap, no podemos establecer TTL directamente
      // En una implementación real, se podría usar RBucket o RExpirable
      log.debug("Value stored in cache successfully: {} (TTL not supported for RMap)", key);
      return null;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .doOnError(error -> log.error("Error storing value in cache with TTL: {}", key, error))
        .onErrorComplete();
  }

  @Override
  public Mono<Void> remove(String key) {
    log.debug("Removing value from cache: {}", key);

    return Mono.fromCallable(() -> {
      RMap<String, Object> cache = redisson.getMap("distributed-cache");
      Object removed = cache.remove(key);
      if (removed != null) {
        log.debug("Value removed from cache successfully: {}", key);
      } else {
        log.debug("Key not found in cache: {}", key);
      }
      return null;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .doOnError(error -> log.error("Error removing value from cache: {}", key, error))
        .onErrorComplete();
  }

  @Override
  public Mono<Boolean> containsKey(String key) {
    log.debug("Checking if cache contains key: {}", key);

    return Mono.fromCallable(() -> {
      RMap<String, Object> cache = redisson.getMap("distributed-cache");
      boolean contains = cache.containsKey(key);
      log.debug("Cache contains key {}: {}", key, contains);
      return contains;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error checking if cache contains key: {}", key, error))
        .onErrorReturn(false);
  }

  @Override
  public Mono<Long> getTtl(String key) {
    log.debug("Getting TTL for cache key: {}", key);

    return Mono.fromCallable(() -> {
      // RMap no soporta TTL directamente
      log.debug("TTL not supported for RMap, returning -1 for key: {}", key);
      return -1L;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error getting TTL for cache key: {}", key, error))
        .onErrorReturn(-1L);
  }

  @Override
  public Mono<Void> expire(String key, long ttl) {
    log.debug("Setting TTL for cache key: {} to {} seconds", key, ttl);

    return Mono.fromCallable(() -> {
      // RMap no soporta TTL directamente
      log.debug("TTL not supported for RMap, ignoring expire request for key: {}", key);
      return null;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .doOnError(error -> log.error("Error setting TTL for cache key: {}", key, error))
        .onErrorComplete();
  }
}