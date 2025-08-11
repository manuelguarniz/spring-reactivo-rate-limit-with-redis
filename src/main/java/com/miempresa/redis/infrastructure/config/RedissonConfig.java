package com.miempresa.redis.infrastructure.config;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de Redisson para funcionalidades avanzadas de Redis
 * Incluye configuración optimizada para rate limiting y programación reactiva
 */
@Slf4j
@Configuration
public class RedissonConfig {

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Value("${redisson.connection-pool-size:64}")
  private int connectionPoolSize;

  @Value("${redisson.connection-minimum-idle:24}")
  private int connectionMinimumIdle;

  @Value("${redisson.lock-watchdog-timeout:30000}")
  private int lockWatchdogTimeout;

  @Value("${redisson.rate-limit.lock-timeout:5000}")
  private int rateLimitLockTimeout;

  @Value("${redisson.rate-limit.cache-ttl:600}")
  private int rateLimitCacheTtl;

  /**
   * Configuración principal de Redisson
   */
  @Bean
  @Primary
  public Config redissonConfig() {
    Config config = new Config();

    // Configuración de servidor único (puede extenderse a cluster)
    config.useSingleServer()
        .setAddress("redis://" + redisHost + ":" + redisPort)
        .setConnectionPoolSize(connectionPoolSize)
        .setConnectionMinimumIdleSize(connectionMinimumIdle)
        .setRetryAttempts(3)
        .setRetryInterval(1500)
        .setTimeout(5000)
        .setConnectTimeout(10000);

    // Configurar password si está presente
    if (redisPassword != null && !redisPassword.isEmpty()) {
      config.useSingleServer().setPassword(redisPassword);
    }

    // Configuración de threads para operaciones reactivas
    config.setThreads(16);
    config.setNettyThreads(32);

    // Configuración de codec para mejor rendimiento
    config.setCodec(new org.redisson.codec.JsonJacksonCodec());

    log.info("Redisson configurado para {}:{} con pool size: {}, min idle: {}, lock timeout: {}ms",
        redisHost, redisPort, connectionPoolSize, connectionMinimumIdle, lockWatchdogTimeout);

    return config;
  }

  /**
   * Configuración específica para rate limiting
   */
  @Bean
  public RedissonRateLimitConfig redissonRateLimitConfig() {
    return RedissonRateLimitConfig.builder()
        .lockTimeout(rateLimitLockTimeout)
        .cacheTtl(rateLimitCacheTtl)
        .build();
  }
}