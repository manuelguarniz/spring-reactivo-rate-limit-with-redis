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

  public boolean isRateLimitEnabled() {
    return enabled;
  }

  public boolean hasReachedLimit(int currentCount) {
    return currentCount >= maxRequests;
  }
}