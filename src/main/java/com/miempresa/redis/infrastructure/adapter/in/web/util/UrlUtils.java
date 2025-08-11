package com.miempresa.redis.infrastructure.adapter.in.web.util;

/**
 * Utilidades para el manejo de URLs y endpoints
 */
public class UrlUtils {

  /**
   * Normaliza un endpoint asegurando que siempre empiece con slash
   * 
   * @param endpoint El endpoint a normalizar
   * @return El endpoint normalizado con slash al inicio
   */
  public static String normalizeEndpoint(String endpoint) {
    if (endpoint == null || endpoint.trim().isEmpty()) {
      return "/";
    }

    String normalized = endpoint.trim();
    if (!normalized.startsWith("/")) {
      normalized = "/" + normalized;
    }

    return normalized;
  }
}