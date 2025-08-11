package com.miempresa.redis.infrastructure.adapter.out.persistence;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Servicio para inicializar datos por defecto en la aplicación
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataService implements CommandLineRunner {

  private final RateLimitUseCase rateLimitUseCase;

  @Override
  public void run(String... args) throws Exception {
    log.info("Initializing default rate limiting configuration...");

    try {
      // Configurar rate limiting por defecto para el endpoint de health
      rateLimitUseCase.updateConfiguration("/api/health", 5, 60, true);
      log.info("Default rate limiting configured for /api/health: 5 requests per minute");

      // Configurar rate limiting por defecto para el endpoint de currency
      rateLimitUseCase.updateConfiguration("/api/currency/convert", 10, 60, true);
      log.info("Default rate limiting configured for /api/currency/convert: 10 requests per minute");

      // El endpoint de tiempo no tiene rate limiting por defecto
      log.info("Default rate limiting configuration completed successfully");

    } catch (Exception e) {
      log.error("Error during default rate limiting configuration", e);
    }
  }
}