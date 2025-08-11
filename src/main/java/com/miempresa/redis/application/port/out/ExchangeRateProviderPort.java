package com.miempresa.redis.application.port.out;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Puerto de salida para obtener tasas de cambio
 * Define cómo el sistema obtiene las tasas de cambio
 */
public interface ExchangeRateProviderPort {

  /**
   * Obtiene la tasa de cambio entre dos monedas
   * 
   * @param sourceCurrency Moneda origen (ej: PEN)
   * @param targetCurrency Moneda destino (ej: USD)
   * @return Mono con la tasa de cambio
   */
  Mono<BigDecimal> getExchangeRate(String sourceCurrency, String targetCurrency);

  /**
   * Obtiene la tasa de cambio por defecto para PEN a USD
   * 
   * @return Mono con la tasa de cambio por defecto
   */
  Mono<BigDecimal> getDefaultPENtoUSDRate();

  /**
   * Obtiene la tasa de cambio por defecto para USD a PEN
   * 
   * @return Mono con la tasa de cambio por defecto
   */
  Mono<BigDecimal> getDefaultUSDtoPENRate();

  /**
   * Valida si una tasa de cambio es válida
   * 
   * @param rate Tasa de cambio a validar
   * @return true si la tasa es válida
   */
  boolean isValidExchangeRate(BigDecimal rate);
}