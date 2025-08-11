package com.miempresa.redis.application.port.in;

import com.miempresa.redis.domain.model.RequestInfo;
import com.miempresa.redis.domain.model.RateLimitConfig;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada reactivo para el caso de uso de rate limiting
 */
public interface RateLimitUseCase {

  /**
   * Verifica si una request está permitida según el rate limiting
   * 
   * @param requestInfo Información de la request
   * @return Mono<Boolean> true si está permitida, false si se excede el límite
   */
  Mono<Boolean> isRequestAllowed(RequestInfo requestInfo);

  /**
   * Actualiza la configuración de rate limiting para un endpoint
   * 
   * @param endpoint          Endpoint a configurar
   * @param maxRequests       Número máximo de requests permitidos
   * @param timeWindowSeconds Ventana de tiempo en segundos
   * @param enabled           Habilitar/deshabilitar rate limiting
   * @return Mono<Void> operación completada
   */
  Mono<Void> updateConfiguration(String endpoint, int maxRequests, int timeWindowSeconds, boolean enabled);

  /**
   * Obtiene la configuración de rate limiting para un endpoint
   * 
   * @param endpoint Endpoint a consultar
   * @return Mono<RateLimitConfig> configuración del rate limiting o empty si no
   *         existe
   */
  Mono<RateLimitConfig> getConfiguration(String endpoint);
}