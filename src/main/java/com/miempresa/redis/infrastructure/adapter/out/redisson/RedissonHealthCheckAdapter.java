package com.miempresa.redis.infrastructure.adapter.out.redisson;

import com.miempresa.redis.application.port.out.HealthCheckPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Adaptador de infraestructura para health checks usando Redisson
 * Implementa el puerto de salida HealthCheckPort
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonHealthCheckAdapter implements HealthCheckPort {

  private final RedissonClient redisson;

  @Override
  public Mono<Boolean> isHealthy() {
    log.debug("Checking Redisson health");

    return Mono.fromCallable(() -> {
      try {
        // Verificar que Redisson esté funcionando
        long totalKeys = redisson.getKeys().count();

        // Verificar conexión básica
        redisson.getKeys().getKeys();

        log.debug("Redisson health check passed - totalKeys: {}", totalKeys);
        return true;

      } catch (Exception e) {
        log.error("Redisson health check failed", e);
        return false;
      }
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error during Redisson health check", error))
        .onErrorReturn(false);
  }

  @Override
  public Mono<HealthInfo> getHealthInfo() {
    log.debug("Getting detailed Redisson health info");

    return Mono.fromCallable(() -> {
      try {
        long totalKeys = redisson.getKeys().count();

        return (HealthInfo) new RedissonHealthInfo(totalKeys);

      } catch (Exception e) {
        log.error("Error getting Redisson health info", e);
        return (HealthInfo) new RedissonHealthInfo(-1);
      }
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error getting Redisson health info", error))
        .onErrorReturn((HealthInfo) new RedissonHealthInfo(-1));
  }

  /**
   * Implementación de HealthInfo para Redisson
   */
  private static class RedissonHealthInfo implements HealthInfo {
    private final long totalKeys;
    private final long timestamp;

    public RedissonHealthInfo(long totalKeys) {
      this.totalKeys = totalKeys;
      this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getStatus() {
      return totalKeys >= 0 ? "UP" : "DOWN";
    }

    @Override
    public String getMessage() {
      if (totalKeys >= 0) {
        return String.format("Redisson is healthy - Total Keys: %d", totalKeys);
      } else {
        return "Redisson health check failed";
      }
    }

    @Override
    public String getDetails() {
      return String.format("Total Keys: %d", totalKeys);
    }

    @Override
    public long getTimestamp() {
      return timestamp;
    }
  }
}