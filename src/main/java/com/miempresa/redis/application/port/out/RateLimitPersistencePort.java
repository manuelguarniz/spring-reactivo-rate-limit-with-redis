package com.miempresa.redis.application.port.out;

import com.miempresa.redis.domain.model.RateLimitConfig;
import com.miempresa.redis.domain.model.RequestInfo;

import java.time.Duration;

/**
 * Puerto de salida para la persistencia del rate limiting
 */
public interface RateLimitPersistencePort {

  /**
   * Obtiene el contador actual de requests para un endpoint e IP
   * 
   * @param requestInfo Información de la request
   * @return Número actual de requests o 0 si no existe
   */
  int getCurrentRequestCount(RequestInfo requestInfo);

  /**
   * Incrementa el contador de requests
   * 
   * @param requestInfo       Información de la request
   * @param timeWindowSeconds Ventana de tiempo para el TTL
   */
  void incrementRequestCount(RequestInfo requestInfo, int timeWindowSeconds);

  /**
   * Obtiene la configuración de rate limiting para un endpoint
   * 
   * @param endpoint Endpoint a consultar
   * @return Configuración o null si no existe
   */
  RateLimitConfig getConfiguration(String endpoint);

  /**
   * Guarda la configuración de rate limiting para un endpoint
   * 
   * @param config Configuración a guardar
   */
  void saveConfiguration(RateLimitConfig config);

  /**
   * Limpia los datos de rate limiting para un endpoint
   * 
   * @param endpoint Endpoint a limpiar
   */
  void clearRateLimitData(String endpoint);
}