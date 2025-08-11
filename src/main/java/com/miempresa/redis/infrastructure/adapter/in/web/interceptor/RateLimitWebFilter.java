package com.miempresa.redis.infrastructure.adapter.in.web.interceptor;

import com.miempresa.redis.application.service.RateLimitServiceFactory;
import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.domain.model.RequestInfo;
import com.miempresa.redis.infrastructure.adapter.in.web.util.ClientIpExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro WebFlux para aplicar rate limiting en endpoints de la API
 * Usa Redisson con fallback automático a implementación original
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitWebFilter implements WebFilter, Ordered {

  private final RateLimitServiceFactory rateLimitServiceFactory;
  private final ClientIpExtractor clientIpExtractor;
  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    // Excluir endpoints de administración del rate limiting
    if (path.startsWith("/api/admin/")) {
      return chain.filter(exchange);
    }

    // Aplicar rate limiting solo a endpoints de API
    if (path.startsWith("/api/")) {
      return applyRateLimiting(exchange, chain, path);
    }

    return chain.filter(exchange);
  }

  /**
   * Aplica rate limiting al request usando el servicio apropiado
   */
  private Mono<Void> applyRateLimiting(ServerWebExchange exchange, WebFilterChain chain, String path) {
    try {
      String clientIp = clientIpExtractor.extractClientIp(exchange);
      String requestId = exchange.getRequest().getId();

      RequestInfo requestInfo = RequestInfo.builder()
          .endpoint(path)
          .clientIp(clientIp)
          .requestId(requestId)
          .build();

      log.debug("Checking rate limit for endpoint: {}, IP: {}, RequestId: {}", path, clientIp, requestId);

      // Obtener el servicio apropiado (distribuido o fallback) de forma reactiva
      return rateLimitServiceFactory.getRateLimitService()
          .flatMap(rateLimitService -> rateLimitService.isRequestAllowed(requestInfo))
          .flatMap(isAllowed -> {
            if (isAllowed) {
              log.debug("Rate limit check passed for endpoint: {}, IP: {}", path, clientIp);
              return chain.filter(exchange);
            } else {
              log.warn("Rate limit exceeded for endpoint: {}, IP: {}", path, clientIp);
              return handleRateLimitExceeded(exchange);
            }
          })
          .onErrorResume(error -> {
            log.error("Error during rate limiting for endpoint: {}", path, error);
            // En caso de error, permitir el request para no bloquear la aplicación
            return chain.filter(exchange);
          });

    } catch (Exception e) {
      log.error("Error during rate limiting for endpoint: {}", path, e);
      // En caso de error, permitir el request para no bloquear la aplicación
      return chain.filter(exchange);
    }
  }

  /**
   * Maneja la respuesta cuando se excede el rate limit
   */
  private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.CONFLICT);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Rate limit exceeded");
    errorResponse.put("message", "Too many requests. Please try again later.");
    errorResponse.put("status", 409);
    errorResponse.put("timestamp", System.currentTimeMillis());

    try {
      String jsonResponse = objectMapper.writeValueAsString(errorResponse);
      byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

      return exchange.getResponse().writeWith(
          Mono.just(exchange.getResponse().bufferFactory().wrap(responseBytes)));
    } catch (Exception e) {
      log.error("Error serializing rate limit error response", e);
      return exchange.getResponse().setComplete();
    }
  }

  @Override
  public int getOrder() {
    // Alta prioridad para asegurar que se ejecute antes que otros filtros
    return Ordered.HIGHEST_PRECEDENCE + 100;
  }
}