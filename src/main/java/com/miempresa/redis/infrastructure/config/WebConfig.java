package com.miempresa.redis.infrastructure.config;

import com.miempresa.redis.infrastructure.adapter.in.web.interceptor.RateLimitWebFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.WebFilter;

/**
 * Configuración web para WebFlux con filtros y otros aspectos de la capa web
 * reactiva
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig {

  private final RateLimitWebFilter rateLimitWebFilter;

  /**
   * Configura el filtro de rate limiting para WebFlux
   * Se aplica a todos los endpoints de API excepto los de administración
   */
  @Bean
  public WebFilter rateLimitFilter() {
    return rateLimitWebFilter;
  }
}