package com.miempresa.redis.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * Health indicator simple para Redisson
 * Monitorea el estado de la conexión de Redisson
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonHealthIndicator {

  private final RedissonClient redisson;

  /**
   * Verifica el estado de Redisson
   * 
   * @return true si está funcionando, false en caso contrario
   */
  public boolean isHealthy() {
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
  }

  /**
   * Obtiene información del estado de Redisson
   * 
   * @return String con información del estado
   */
  public String getHealthInfo() {
    try {
      long totalKeys = redisson.getKeys().count();
      return String.format("Redisson: OK, Total Keys: %d", totalKeys);
    } catch (Exception e) {
      return String.format("Redisson: ERROR, %s", e.getMessage());
    }
  }
}