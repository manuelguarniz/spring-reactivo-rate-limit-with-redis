package com.miempresa.redis.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para health checks
 * Define cómo el sistema verifica el estado de salud de los servicios
 */
public interface HealthCheckPort {

  /**
   * Verifica el estado de salud del servicio
   * 
   * @return Mono con true si el servicio está saludable, false en caso contrario
   */
  Mono<Boolean> isHealthy();

  /**
   * Obtiene información detallada del estado de salud
   * 
   * @return Mono con información del estado de salud
   */
  Mono<HealthInfo> getHealthInfo();

  /**
   * Información del estado de salud
   */
  interface HealthInfo {
    /**
     * Estado general del servicio
     */
    String getStatus();

    /**
     * Mensaje descriptivo del estado
     */
    String getMessage();

    /**
     * Detalles adicionales del estado
     */
    String getDetails();

    /**
     * Timestamp del último health check
     */
    long getTimestamp();
  }
}