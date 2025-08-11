package com.miempresa.redis.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Modelo de dominio para la configuración de rate limiting
 */
@Data
@Builder
public class RateLimitConfig {
  private String endpoint;
  private int maxRequests;
  private int timeWindowSeconds;
  private boolean enabled;

  @Builder.Default
  private int lockTimeout = 5000; // Timeout por defecto en milisegundos

  public boolean isRateLimitEnabled() {
    return enabled;
  }

  public boolean hasReachedLimit(int currentCount) {
    return currentCount >= maxRequests;
  }
}