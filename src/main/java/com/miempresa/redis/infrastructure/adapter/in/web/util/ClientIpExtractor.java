package com.miempresa.redis.infrastructure.adapter.in.web.util;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * Utilidad para extraer la IP real del cliente desde los headers HTTP en
 * WebFlux
 */
@Component
public class ClientIpExtractor {

  /**
   * Extrae la IP real del cliente considerando headers de proxy
   * 
   * @param exchange El exchange de WebFlux
   * @return La IP real del cliente
   */
  public String extractClientIp(ServerWebExchange exchange) {
    HttpHeaders headers = exchange.getRequest().getHeaders();

    // Verificar X-Forwarded-For (usado por proxies y load balancers)
    String xForwardedFor = headers.getFirst("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
      // Tomar la primera IP del header (la IP original del cliente)
      return xForwardedFor.split(",")[0].trim();
    }

    // Verificar X-Real-IP (usado por nginx)
    String xRealIp = headers.getFirst("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
      return xRealIp;
    }

    // Verificar X-Client-IP (usado por algunos proxies)
    String xClientIp = headers.getFirst("X-Client-IP");
    if (xClientIp != null && !xClientIp.isEmpty() && !"unknown".equalsIgnoreCase(xClientIp)) {
      return xClientIp;
    }

    // Verificar CF-Connecting-IP (usado por Cloudflare)
    String cfConnectingIp = headers.getFirst("CF-Connecting-IP");
    if (cfConnectingIp != null && !cfConnectingIp.isEmpty() && !"unknown".equalsIgnoreCase(cfConnectingIp)) {
      return cfConnectingIp;
    }

    // Si no hay headers de proxy, usar la IP remota desde el exchange
    String remoteAddress = exchange.getRequest().getRemoteAddress() != null
        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        : "unknown";

    return remoteAddress;
  }
}