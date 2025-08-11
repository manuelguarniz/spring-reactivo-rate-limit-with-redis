package com.miempresa.redis.infrastructure.config;

import lombok.Builder;
import lombok.Data;

/**
 * Configuración específica para rate limiting con Redisson
 */
@Data
@Builder
public class RedissonRateLimitConfig {

  /**
   * Timeout para locks de rate limiting en milisegundos
   */
  private int lockTimeout;

  /**
   * TTL para cache de configuraciones en segundos
   */
  private int cacheTtl;

  /**
   * Tamaño máximo del cache local
   */
  @Builder.Default
  private int maxCacheSize = 1000;

  /**
   * Política de evicción del cache
   */
  @Builder.Default
  private String evictionPolicy = "LRU";
}