package com.miempresa.redis.util;

/**
 * Clase de configuración de datos de prueba para UrlUtils
 * Centraliza todos los datos de prueba para facilitar el mantenimiento
 */
public final class UrlUtilsTestData {

  // Constructor privado para evitar instanciación
  private UrlUtilsTestData() {
  }

  // Datos de prueba para normalizeEndpoint
  public static final class NormalizeEndpointData {

    // Casos que deben retornar "/"
    public static final String[] NULL_EMPTY_WHITESPACE = {
        null, "", "   ", "  "
    };

    // Casos que deben agregar slash inicial
    public static final String[][] ADD_SLASH_CASES = {
        { "api/health", "/api/health" },
        { "  api/health  ", "/api/health" },
        { "a", "/a" },
        { "api/v1/users/profile", "/api/v1/users/profile" },
        { "users", "/users" },
        { "v1/api", "/v1/api" }
    };

    // Casos que deben mantener slash existente
    public static final String[][] KEEP_SLASH_CASES = {
        { "/api/health", "/api/health" },
        { "  /api/health  ", "/api/health" },
        { "/a", "/a" },
        { "/", "/" },
        { "/api/v1/users/profile", "/api/v1/users/profile" },
        { "  /  ", "/" }
    };
  }

  // Datos de prueba para casos edge
  public static final class EdgeCaseData {

    // Casos con caracteres especiales y números
    public static final String[][] SPECIAL_CHARACTERS_CASES = {
        { "/api/v1/users/123-456_789/profile", "/api/v1/users/123-456_789/profile" },
        { "/api/v1/users/123/profile", "/api/v1/users/123/profile" },
        { "  /api/v1/users/123-456_789/profile  ", "/api/v1/users/123-456_789/profile" },
        { "/api/v1/users/user_123/profile", "/api/v1/users/user_123/profile" },
        { "/api/v1/users/user-123_profile", "/api/v1/users/user-123_profile" }
    };
  }
}