package com.miempresa.redis.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para conversiones de moneda
 */
@Data
@Builder
public class CurrencyConversion {

  /**
   * Monto original en la moneda origen
   */
  private BigDecimal originalAmount;

  /**
   * Moneda origen (ej: PEN)
   */
  private String sourceCurrency;

  /**
   * Moneda destino (ej: USD)
   */
  private String targetCurrency;

  /**
   * Monto convertido en la moneda destino
   */
  private BigDecimal convertedAmount;

  /**
   * Tasa de cambio utilizada
   */
  private BigDecimal exchangeRate;

  /**
   * Timestamp de la conversión
   */
  private LocalDateTime timestamp;

  /**
   * Valida que la conversión sea válida
   */
  public boolean isValid() {
    return originalAmount != null &&
        originalAmount.compareTo(BigDecimal.ZERO) > 0 &&
        exchangeRate != null &&
        exchangeRate.compareTo(BigDecimal.ZERO) > 0 &&
        convertedAmount != null &&
        convertedAmount.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Calcula la tasa de cambio inversa
   */
  public BigDecimal getInverseExchangeRate() {
    if (exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
      return BigDecimal.ONE.divide(exchangeRate, 4, java.math.RoundingMode.HALF_UP);
    }
    return null;
  }
}