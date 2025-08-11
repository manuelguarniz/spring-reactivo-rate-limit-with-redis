package com.miempresa.redis.infrastructure.adapter.in.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utilidad para extraer la IP real del cliente desde los headers HTTP
 */
@Component
public class ClientIpExtractor {

  /**
   * Extrae la IP real del cliente considerando headers de proxy
   * 
   * @param request La request HTTP
   * @return La IP real del cliente
   */
  public String extractClientIp(HttpServletRequest request) {
    // Verificar X-Forwarded-For (usado por proxies y load balancers)
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
      // Tomar la primera IP del header (la IP original del cliente)
      return xForwardedFor.split(",")[0].trim();
    }

    // Verificar X-Real-IP (usado por nginx)
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
      return xRealIp;
    }

    // Verificar X-Client-IP (usado por algunos proxies)
    String xClientIp = request.getHeader("X-Client-IP");
    if (xClientIp != null && !xClientIp.isEmpty() && !"unknown".equalsIgnoreCase(xClientIp)) {
      return xClientIp;
    }

    // Verificar CF-Connecting-IP (usado por Cloudflare)
    String cfConnectingIp = request.getHeader("CF-Connecting-IP");
    if (cfConnectingIp != null && !cfConnectingIp.isEmpty() && !"unknown".equalsIgnoreCase(cfConnectingIp)) {
      return cfConnectingIp;
    }

    // Si no hay headers de proxy, usar la IP remota
    return request.getRemoteAddr();
  }
}