package com.miempresa.redis.application.port.in;

import com.miempresa.redis.domain.model.RequestInfo;

/**
 * Puerto de entrada para el caso de uso de rate limiting
 */
public interface RateLimitUseCase {

  /**
   * Verifica si una request está permitida según el rate limiting
   * 
   * @param requestInfo Información de la request
   * @return true si está permitida, false si se excede el límite
   */
  boolean isRequestAllowed(RequestInfo requestInfo);

  /**
   * Actualiza la configuración de rate limiting para un endpoint
   * 
   * @param endpoint          Endpoint a configurar
   * @param maxRequests       Número máximo de requests permitidos
   * @param timeWindowSeconds Ventana de tiempo en segundos
   * @param enabled           Habilitar/deshabilitar rate limiting
   */
  void updateConfiguration(String endpoint, int maxRequests, int timeWindowSeconds, boolean enabled);

  /**
   * Obtiene la configuración de rate limiting para un endpoint
   * 
   * @param endpoint Endpoint a consultar
   * @return Configuración del rate limiting o null si no existe
   */
  com.miempresa.redis.domain.model.RateLimitConfig getConfiguration(String endpoint);
}