package com.miempresa.redis.domain.service.impl;

import com.miempresa.redis.domain.service.UrlNormalizationService;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de normalización de URLs
 */
@Service
public class UrlNormalizationServiceImpl implements UrlNormalizationService {

  @Override
  public String normalizeEndpoint(String endpoint) {
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