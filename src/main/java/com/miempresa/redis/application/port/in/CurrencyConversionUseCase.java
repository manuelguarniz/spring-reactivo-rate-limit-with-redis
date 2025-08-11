package com.miempresa.redis.application.port.in;

import com.miempresa.redis.domain.model.CurrencyConversion;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Puerto de entrada para el caso de uso de conversión de moneda
 * Define las operaciones que puede realizar el sistema de conversión
 */
public interface CurrencyConversionUseCase {

  /**
   * Convierte un monto de una moneda a otra
   * 
   * @param amount         Monto a convertir
   * @param sourceCurrency Moneda origen (ej: PEN)
   * @param targetCurrency Moneda destino (ej: USD)
   * @param customRate     Tasa de cambio personalizada (opcional)
   * @return Mono con la conversión realizada
   */
  Mono<CurrencyConversion> convertCurrency(
      BigDecimal amount,
      String sourceCurrency,
      String targetCurrency,
      BigDecimal customRate);

  /**
   * Convierte un monto de PEN a USD usando la tasa por defecto
   * 
   * @param amountInPEN Monto en PEN
   * @return Mono con la conversión a USD
   */
  Mono<CurrencyConversion> convertPENtoUSD(BigDecimal amountInPEN);

  /**
   * Convierte un monto de USD a PEN usando la tasa por defecto
   * 
   * @param amountInUSD Monto en USD
   * @return Mono con la conversión a PEN
   */
  Mono<CurrencyConversion> convertUSDtoPEN(BigDecimal amountInUSD);

  /**
   * Obtiene la tasa de cambio actual entre dos monedas
   * 
   * @param sourceCurrency Moneda origen
   * @param targetCurrency Moneda destino
   * @return Mono con la tasa de cambio
   */
  Mono<BigDecimal> getExchangeRate(String sourceCurrency, String targetCurrency);
}