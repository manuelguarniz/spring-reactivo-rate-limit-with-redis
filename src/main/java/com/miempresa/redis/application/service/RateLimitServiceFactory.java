package com.miempresa.redis.application.service;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.application.port.out.HealthCheckPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Factory para crear el servicio de rate limiting apropiado
 * Permite fallback automático entre implementación distribuida y original
 * Sigue arquitectura hexagonal usando puertos de salida
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitServiceFactory {

  private final RedissonRateLimitService distributedService;
  private final RateLimitService fallbackService;
  private final HealthCheckPort healthCheckPort;

  /**
   * Obtiene el servicio de rate limiting apropiado
   * 
   * @return RateLimitUseCase implementación
   */
  public Mono<RateLimitUseCase> getRateLimitService() {
    return healthCheckPort.isHealthy()
        .map(isHealthy -> {
          if (isHealthy) {
            log.debug("Using distributed rate limiting service");
            return distributedService;
          } else {
            log.warn("Distributed service not healthy, falling back to original implementation");
            return fallbackService;
          }
        })
        .defaultIfEmpty(fallbackService); // Fallback por defecto si no se puede determinar el estado
  }

  /**
   * Obtiene el servicio de rate limiting apropiado de forma síncrona
   * (para compatibilidad con código existente)
   * 
   * @return RateLimitUseCase implementación
   */
  public RateLimitUseCase getRateLimitServiceSync() {
    try {
      Boolean isHealthy = healthCheckPort.isHealthy().block();
      if (isHealthy != null && isHealthy) {
        log.debug("Using distributed rate limiting service");
        return distributedService;
      } else {
        log.warn("Distributed service not healthy, using fallback");
        return fallbackService;
      }
    } catch (Exception e) {
      log.error("Error checking service health, using fallback", e);
      return fallbackService;
    }
  }

  /**
   * Fuerza el uso del servicio de fallback
   * 
   * @return RateLimitUseCase implementación de fallback
   */
  public RateLimitUseCase getFallbackService() {
    log.info("Forcing fallback to original rate limiting implementation");
    return fallbackService;
  }

  /**
   * Fuerza el uso del servicio distribuido
   * 
   * @return RateLimitUseCase implementación distribuida
   */
  public RateLimitUseCase getDistributedService() {
    log.info("Forcing distributed rate limiting service");
    return distributedService;
  }

  /**
   * Obtiene información del estado de salud del servicio distribuido
   * 
   * @return Mono con información del estado de salud
   */
  public Mono<HealthCheckPort.HealthInfo> getServiceHealthInfo() {
    return healthCheckPort.getHealthInfo();
  }
}