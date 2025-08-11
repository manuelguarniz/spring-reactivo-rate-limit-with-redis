package com.miempresa.redis.application.port.out;

import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Puerto de salida reactivo para la persistencia del rate limiting
 */
public interface RateLimitPersistencePort {

  /**
   * Obtiene el contador actual de requests para un endpoint e IP
   * 
   * @param requestInfo Información de la request
   * @return Mono<Integer> número actual de requests o 0 si no existe
   */
  Mono<Integer> getCurrentRequestCount(RequestInfo requestInfo);

  /**
   * Incrementa el contador de requests
   * 
   * @param requestInfo       Información de la request
   * @param timeWindowSeconds Ventana de tiempo para el TTL
   * @return Mono<Void> operación completada
   */
  Mono<Void> incrementRequestCount(RequestInfo requestInfo, int timeWindowSeconds);

  /**
   * Obtiene la configuración de rate limiting para un endpoint
   * 
   * @param endpoint Endpoint a consultar
   * @return Mono<RateLimitConfig> configuración o empty si no existe
   */
  Mono<RateLimitConfig> getConfiguration(String endpoint);

  /**
   * Guarda la configuración de rate limiting para un endpoint
   * 
   * @param config Configuración a guardar
   * @return Mono<Void> operación completada
   */
  Mono<Void> saveConfiguration(RateLimitConfig config);

  /**
   * Limpia los datos de rate limiting para un endpoint
   * 
   * @param endpoint Endpoint a limpiar
   * @return Mono<Void> operación completada
   */
  Mono<Void> clearRateLimitData(String endpoint);
}