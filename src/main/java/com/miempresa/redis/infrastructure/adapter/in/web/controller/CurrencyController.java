package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import com.miempresa.redis.application.port.in.CurrencyConversionUseCase;
import com.miempresa.redis.domain.model.CurrencyConversion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador reactivo para operaciones de conversión de moneda
 * Usa el caso de uso de conversión de moneda siguiendo arquitectura hexagonal
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CurrencyController {

  private final CurrencyConversionUseCase currencyConversionUseCase;

  @GetMapping("/currency/convert")
  public Mono<ResponseEntity<Map<String, Object>>> convertSolesToDollars(
      @RequestParam("amount") BigDecimal amount,
      @RequestParam(value = "rate", required = false) BigDecimal customRate) {

    log.debug("Currency conversion request - amount: {}, customRate: {}", amount, customRate);

    // Usar el caso de uso para realizar la conversión
    return currencyConversionUseCase.convertCurrency(amount, "PEN", "USD", customRate)
        .map(this::buildSuccessResponse)
        .onErrorResume(IllegalArgumentException.class, error -> {
          log.warn("Invalid amount for currency conversion: {}", amount);
          return Mono.just(buildErrorResponse(error.getMessage(), 400));
        })
        .onErrorResume(Exception.class, error -> {
          log.error("Error during currency conversion for amount: {}", amount, error);
          return Mono.just(buildErrorResponse("Error en el cálculo: " + error.getMessage(), 500));
        });
  }

  @GetMapping("/currency/convert/pen-to-usd")
  public Mono<ResponseEntity<Map<String, Object>>> convertPENtoUSD(
      @RequestParam("amount") BigDecimal amount) {

    log.debug("PEN to USD conversion request - amount: {}", amount);

    return currencyConversionUseCase.convertPENtoUSD(amount)
        .map(this::buildSuccessResponse)
        .onErrorResume(IllegalArgumentException.class, error -> {
          log.warn("Invalid amount for PEN to USD conversion: {}", amount);
          return Mono.just(buildErrorResponse(error.getMessage(), 400));
        })
        .onErrorResume(Exception.class, error -> {
          log.error("Error during PEN to USD conversion for amount: {}", amount, error);
          return Mono.just(buildErrorResponse("Error en el cálculo: " + error.getMessage(), 500));
        });
  }

  @GetMapping("/currency/convert/usd-to-pen")
  public Mono<ResponseEntity<Map<String, Object>>> convertUSDtoPEN(
      @RequestParam("amount") BigDecimal amount) {

    log.debug("USD to PEN conversion request - amount: {}", amount);

    return currencyConversionUseCase.convertUSDtoPEN(amount)
        .map(this::buildSuccessResponse)
        .onErrorResume(IllegalArgumentException.class, error -> {
          log.warn("Invalid amount for USD to PEN conversion: {}", amount);
          return Mono.just(buildErrorResponse(error.getMessage(), 400));
        })
        .onErrorResume(Exception.class, error -> {
          log.error("Error during USD to PEN conversion for amount: {}", amount, error);
          return Mono.just(buildErrorResponse("Error en el cálculo: " + error.getMessage(), 500));
        });
  }

  @GetMapping("/currency/exchange-rate")
  public Mono<ResponseEntity<Map<String, Object>>> getExchangeRate(
      @RequestParam("from") String sourceCurrency,
      @RequestParam("to") String targetCurrency) {

    log.debug("Exchange rate request - from: {} to: {}", sourceCurrency, targetCurrency);

    return currencyConversionUseCase.getExchangeRate(sourceCurrency, targetCurrency)
        .map(rate -> buildExchangeRateResponse(sourceCurrency, targetCurrency, rate))
        .onErrorResume(IllegalArgumentException.class, error -> {
          log.warn("Invalid exchange rate request: {} to {}", sourceCurrency, targetCurrency);
          return Mono.just(buildErrorResponse(error.getMessage(), 400));
        })
        .onErrorResume(Exception.class, error -> {
          log.error("Error getting exchange rate from {} to {}", sourceCurrency, targetCurrency, error);
          return Mono.just(buildErrorResponse("Error obteniendo tasa de cambio: " + error.getMessage(), 500));
        });
  }

  /**
   * Construye la respuesta de éxito para una conversión
   */
  private ResponseEntity<Map<String, Object>> buildSuccessResponse(CurrencyConversion conversion) {
    Map<String, Object> response = new HashMap<>();
    response.put("originalAmount", conversion.getOriginalAmount());
    response.put("originalCurrency", conversion.getSourceCurrency());
    response.put("convertedAmount", conversion.getConvertedAmount());
    response.put("targetCurrency", conversion.getTargetCurrency());
    response.put("exchangeRate", conversion.getExchangeRate());
    response.put("timestamp", conversion.getTimestamp());
    response.put("message", "Conversión exitosa");

    return ResponseEntity.ok(response);
  }

  /**
   * Construye la respuesta de error
   */
  private ResponseEntity<Map<String, Object>> buildErrorResponse(String errorMessage, int statusCode) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", errorMessage);
    response.put("status", statusCode);
    response.put("timestamp", System.currentTimeMillis());

    if (statusCode == 400) {
      return ResponseEntity.badRequest().body(response);
    } else {
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Construye la respuesta para consulta de tasa de cambio
   */
  private ResponseEntity<Map<String, Object>> buildExchangeRateResponse(
      String sourceCurrency, String targetCurrency, BigDecimal rate) {

    Map<String, Object> response = new HashMap<>();
    response.put("sourceCurrency", sourceCurrency);
    response.put("targetCurrency", targetCurrency);
    response.put("exchangeRate", rate);
    response.put("message", "Tasa de cambio obtenida exitosamente");
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }
}