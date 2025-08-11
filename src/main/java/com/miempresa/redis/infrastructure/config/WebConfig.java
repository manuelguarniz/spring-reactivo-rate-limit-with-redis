package com.miempresa.redis.infrastructure.config;

import com.miempresa.redis.infrastructure.adapter.in.web.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web para interceptores y otros aspectos de la capa web
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final RateLimitInterceptor rateLimitInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor)
        .addPathPatterns("/api/**") // Aplicar a todos los endpoints de API
        .excludePathPatterns("/api/admin/**"); // Excluir endpoints de administración del rate limiting
  }
}