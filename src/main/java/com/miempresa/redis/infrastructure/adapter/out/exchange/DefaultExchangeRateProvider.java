package com.miempresa.redis.infrastructure.adapter.out.exchange;

import com.miempresa.redis.application.port.out.ExchangeRateProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador de infraestructura para proveer tasas de cambio
 * Implementa el puerto de salida ExchangeRateProviderPort
 */
@Slf4j
@Component
public class DefaultExchangeRateProvider implements ExchangeRateProviderPort {

  // Tasa de cambio fija por defecto (en un caso real, esto vendría de un servicio
  // externo)
  private static final BigDecimal DEFAULT_PEN_TO_USD_RATE = new BigDecimal("3.8");
  private static final BigDecimal DEFAULT_USD_TO_PEN_RATE = new BigDecimal("0.2632");

  // Cache simple de tasas de cambio (en un caso real, esto podría usar Redis)
  private final Map<String, BigDecimal> exchangeRates = new HashMap<>();

  public DefaultExchangeRateProvider() {
    // Inicializar tasas por defecto
    exchangeRates.put("PEN:USD", DEFAULT_PEN_TO_USD_RATE);
    exchangeRates.put("USD:PEN", DEFAULT_USD_TO_PEN_RATE);

    log.info("Default exchange rates initialized - PEN:USD = {}, USD:PEN = {}",
        DEFAULT_PEN_TO_USD_RATE, DEFAULT_USD_TO_PEN_RATE);
  }

  @Override
  public Mono<BigDecimal> getExchangeRate(String sourceCurrency, String targetCurrency) {
    String rateKey = sourceCurrency + ":" + targetCurrency;

    log.debug("Getting exchange rate for {}:{}", sourceCurrency, targetCurrency);

    return Mono.fromCallable(() -> {
      BigDecimal rate = exchangeRates.get(rateKey);
      if (rate == null) {
        log.warn("Exchange rate not found for {}:{}", sourceCurrency, targetCurrency);
        throw new IllegalArgumentException(
            "Tasa de cambio no disponible para " + sourceCurrency + " a " + targetCurrency);
      }
      return rate;
    })
        .doOnSuccess(rate -> log.debug("Exchange rate found for {}:{} = {}", sourceCurrency, targetCurrency, rate))
        .doOnError(error -> log.error("Error getting exchange rate for {}:{}", sourceCurrency, targetCurrency, error));
  }

  @Override
  public Mono<BigDecimal> getDefaultPENtoUSDRate() {
    log.debug("Getting default PEN to USD rate");
    return Mono.just(DEFAULT_PEN_TO_USD_RATE);
  }

  @Override
  public Mono<BigDecimal> getDefaultUSDtoPENRate() {
    log.debug("Getting default USD to PEN rate");
    return Mono.just(DEFAULT_USD_TO_PEN_RATE);
  }

  @Override
  public boolean isValidExchangeRate(BigDecimal rate) {
    return rate != null && rate.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Actualiza una tasa de cambio (método para testing o configuración dinámica)
   */
  public void updateExchangeRate(String sourceCurrency, String targetCurrency, BigDecimal newRate) {
    if (!isValidExchangeRate(newRate)) {
      throw new IllegalArgumentException("La tasa de cambio debe ser mayor a 0");
    }

    String rateKey = sourceCurrency + ":" + targetCurrency;
    exchangeRates.put(rateKey, newRate);

    log.info("Exchange rate updated for {}:{} = {}", sourceCurrency, targetCurrency, newRate);
  }

  /**
   * Obtiene todas las tasas de cambio disponibles
   */
  public Map<String, BigDecimal> getAllExchangeRates() {
    return new HashMap<>(exchangeRates);
  }
}