package com.miempresa.redis.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Modelo de dominio para la información de una request
 */
@Data
@Builder
public class RequestInfo {
  private String endpoint;
  private String clientIp;
  private String requestId;

  public String getRateLimitKey() {
    return "rate-limit:" + endpoint + ":" + clientIp;
  }

  public String getConfigKey() {
    return "rate-limit:config:" + endpoint;
  }
}