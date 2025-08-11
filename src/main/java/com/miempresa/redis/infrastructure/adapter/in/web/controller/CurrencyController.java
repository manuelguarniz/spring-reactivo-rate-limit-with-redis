package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para operaciones de conversión de moneda
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class CurrencyController {

  // Tasa de cambio fija (en un caso real, esto vendría de un servicio externo)
  private static final BigDecimal EXCHANGE_RATE = new BigDecimal("3.8"); // 1 USD = 3.8 PEN

  @GetMapping("/currency/convert")
  public ResponseEntity<Map<String, Object>> convertSolesToDollars(
      @RequestParam("amount") BigDecimal amount,
      @RequestParam(value = "rate", required = false) BigDecimal customRate) {

    log.debug("Currency conversion request - amount: {}, customRate: {}", amount, customRate);

    Map<String, Object> response = new HashMap<>();

    try {
      // Validar que el monto sea positivo
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("Invalid amount for currency conversion: {}", amount);
        response.put("error", "El monto debe ser mayor a 0");
        return ResponseEntity.badRequest().body(response);
      }

      // Usar tasa personalizada si se proporciona, sino usar la tasa por defecto
      BigDecimal exchangeRate = customRate != null ? customRate : EXCHANGE_RATE;

      // Calcular la conversión: PEN / rate = USD
      // Ejemplo: 100 PEN / 3.8 = 26.32 USD
      BigDecimal dollars = amount.divide(exchangeRate, 2, RoundingMode.HALF_UP);

      // Construir respuesta
      response.put("originalAmount", amount);
      response.put("originalCurrency", "PEN");
      response.put("convertedAmount", dollars);
      response.put("targetCurrency", "USD");
      response.put("exchangeRate", exchangeRate);
      response.put("timestamp",
          java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

      log.debug("Currency conversion successful - {} PEN = {} USD", amount, dollars);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error during currency conversion for amount: {}", amount, e);
      response.put("error", "Error en el cálculo: " + e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }
}