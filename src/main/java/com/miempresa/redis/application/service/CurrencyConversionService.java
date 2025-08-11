package com.miempresa.redis.application.service;

import com.miempresa.redis.application.port.in.CurrencyConversionUseCase;
import com.miempresa.redis.application.port.out.ExchangeRateProviderPort;
import com.miempresa.redis.domain.model.CurrencyConversion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Servicio de aplicación para conversiones de moneda
 * Implementa el caso de uso de conversión de moneda siguiendo arquitectura
 * hexagonal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConversionService implements CurrencyConversionUseCase {

  private final ExchangeRateProviderPort exchangeRateProvider;

  @Override
  public Mono<CurrencyConversion> convertCurrency(
      BigDecimal amount,
      String sourceCurrency,
      String targetCurrency,
      BigDecimal customRate) {

    log.debug("Converting currency: {} {} to {} with custom rate: {}",
        amount, sourceCurrency, targetCurrency, customRate);

    return Mono.justOrEmpty(amount)
        .filter(amt -> amt.compareTo(BigDecimal.ZERO) > 0)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El monto debe ser mayor a 0")))
        .flatMap(validAmount -> {
          if (customRate != null) {
            return validateAndUseCustomRate(validAmount, sourceCurrency, targetCurrency, customRate);
          } else {
            return getExchangeRateAndConvert(validAmount, sourceCurrency, targetCurrency);
          }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(conversion -> log.debug("Currency conversion successful: {} {} = {} {}",
            conversion.getOriginalAmount(), conversion.getSourceCurrency(),
            conversion.getConvertedAmount(), conversion.getTargetCurrency()))
        .doOnError(error -> log.error("Error during currency conversion: {} {} to {}",
            amount, sourceCurrency, targetCurrency, error));
  }

  @Override
  public Mono<CurrencyConversion> convertPENtoUSD(BigDecimal amountInPEN) {
    log.debug("Converting PEN to USD: {}", amountInPEN);
    return convertCurrency(amountInPEN, "PEN", "USD", null);
  }

  @Override
  public Mono<CurrencyConversion> convertUSDtoPEN(BigDecimal amountInUSD) {
    log.debug("Converting USD to PEN: {}", amountInUSD);
    return convertCurrency(amountInUSD, "USD", "PEN", null);
  }

  @Override
  public Mono<BigDecimal> getExchangeRate(String sourceCurrency, String targetCurrency) {
    log.debug("Getting exchange rate from {} to {}", sourceCurrency, targetCurrency);
    return exchangeRateProvider.getExchangeRate(sourceCurrency, targetCurrency)
        .doOnSuccess(rate -> log.debug("Exchange rate obtained: {} {} = {} {}",
            sourceCurrency, targetCurrency, BigDecimal.ONE, rate))
        .doOnError(error -> log.error("Error getting exchange rate from {} to {}",
            sourceCurrency, targetCurrency, error));
  }

  /**
   * Valida y usa una tasa de cambio personalizada
   */
  private Mono<CurrencyConversion> validateAndUseCustomRate(
      BigDecimal amount,
      String sourceCurrency,
      String targetCurrency,
      BigDecimal customRate) {

    if (!exchangeRateProvider.isValidExchangeRate(customRate)) {
      return Mono.error(new IllegalArgumentException("La tasa de cambio personalizada no es válida"));
    }

    return Mono.fromCallable(() -> performConversion(amount, sourceCurrency, targetCurrency, customRate))
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * Obtiene la tasa de cambio y realiza la conversión
   */
  private Mono<CurrencyConversion> getExchangeRateAndConvert(
      BigDecimal amount,
      String sourceCurrency,
      String targetCurrency) {

    return exchangeRateProvider.getExchangeRate(sourceCurrency, targetCurrency)
        .flatMap(rate -> Mono.fromCallable(() -> performConversion(amount, sourceCurrency, targetCurrency, rate))
            .subscribeOn(Schedulers.boundedElastic()));
  }

  /**
   * Realiza la conversión de moneda
   */
  private CurrencyConversion performConversion(
      BigDecimal amount,
      String sourceCurrency,
      String targetCurrency,
      BigDecimal exchangeRate) {

    // Calcular la conversión
    BigDecimal convertedAmount = amount.divide(exchangeRate, 2, RoundingMode.HALF_UP);

    // Construir el resultado
    return CurrencyConversion.builder()
        .originalAmount(amount)
        .sourceCurrency(sourceCurrency)
        .targetCurrency(targetCurrency)
        .convertedAmount(convertedAmount)
        .exchangeRate(exchangeRate)
        .timestamp(LocalDateTime.now())
        .build();
  }
}