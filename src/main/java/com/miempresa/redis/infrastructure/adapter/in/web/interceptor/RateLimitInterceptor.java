package com.miempresa.redis.infrastructure.adapter.in.web.interceptor;

import com.miempresa.redis.application.port.in.RateLimitUseCase;
import com.miempresa.redis.domain.model.RequestInfo;
import com.miempresa.redis.infrastructure.adapter.in.web.util.ClientIpExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Interceptor para aplicar rate limiting usando la arquitectura hexagonal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

  private final RateLimitUseCase rateLimitUseCase;
  private final ClientIpExtractor clientIpExtractor;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {

    String requestURI = request.getRequestURI();
    String clientIp = clientIpExtractor.extractClientIp(request);
    String requestId = UUID.randomUUID().toString();

    log.debug("=== RateLimitInterceptor triggered ===");
    log.debug("Request URI: {}", requestURI);
    log.debug("Client IP: {}", clientIp);
    log.debug("Request ID: {}", requestId);
    log.debug("Handler: {}", handler.getClass().getSimpleName());

    // Crear objeto de dominio para la request
    RequestInfo requestInfo = RequestInfo.builder()
        .endpoint(requestURI)
        .clientIp(clientIp)
        .requestId(requestId)
        .build();

    // Verificar rate limiting usando el caso de uso
    if (rateLimitUseCase.isRequestAllowed(requestInfo)) {
      log.debug("Rate limit check: ALLOWED for request ID: {}", requestId);
      return true;
    } else {
      log.warn("Rate limit check: BLOCKED for request ID: {}", requestId);

      // Rate limit exceeded - return 409 Conflict
      response.setStatus(HttpStatus.CONFLICT.value());
      response.setContentType("application/json");

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Rate limit exceeded");
      errorResponse.put("message", "Too many requests for this endpoint");
      errorResponse.put("endpoint", requestURI);
      errorResponse.put("clientIp", clientIp);
      errorResponse.put("requestId", requestId);

      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      return false;
    }
  }
}