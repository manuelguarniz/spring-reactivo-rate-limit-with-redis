package com.miempresa.redis.domain.service;

/**
 * Servicio de dominio para normalización de URLs
 */
public interface UrlNormalizationService {

  /**
   * Normaliza un endpoint asegurando que siempre empiece con slash
   * 
   * @param endpoint El endpoint a normalizar
   * @return El endpoint normalizado con slash al inicio
   */
  String normalizeEndpoint(String endpoint);
}