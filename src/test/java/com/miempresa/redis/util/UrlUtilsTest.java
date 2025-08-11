package com.miempresa.redis.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.miempresa.redis.infrastructure.adapter.in.web.util.UrlUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitarios para la clase UrlUtils
 */
@DisplayName("UrlUtils Tests")
class UrlUtilsTest {

  @Nested
  @DisplayName("normalizeEndpoint Tests")
  class NormalizeEndpointTests {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "  " })
    @DisplayName("Should return '/' for null, empty or whitespace only strings")
    void shouldReturnSlashForNullOrEmptyOrWhitespace(String input) {
      String result = UrlUtils.normalizeEndpoint(input);
      assertEquals("/", result);
    }

    @ParameterizedTest
    @CsvSource({
        "api/health, /api/health",
        "  api/health  , /api/health",
        "a, /a",
        "api/v1/users/profile, /api/v1/users/profile"
    })
    @DisplayName("Should add leading slash for endpoint without slash")
    void shouldAddLeadingSlashForEndpointWithoutSlash(String input, String expected) {
      String result = UrlUtils.normalizeEndpoint(input);
      assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "/api/health, /api/health",
        "  /api/health  , /api/health",
        "/a, /a",
        "/, /",
        "/api/v1/users/profile, /api/v1/users/profile"
    })
    @DisplayName("Should keep existing slash for endpoint with slash")
    void shouldKeepExistingSlashForEndpointWithSlash(String input, String expected) {
      String result = UrlUtils.normalizeEndpoint(input);
      assertEquals(expected, result);
    }
  }

  @Nested
  @DisplayName("Edge Cases Tests")
  class EdgeCasesTests {

    @ParameterizedTest
    @CsvSource({
        "/api/v1/users/123-456_789/profile, /api/v1/users/123-456_789/profile",
        "/api/v1/users/123/profile, /api/v1/users/123/profile",
        "  /api/v1/users/123-456_789/profile  , /api/v1/users/123-456_789/profile"
    })
    @DisplayName("Should handle endpoints with special characters and numbers")
    void shouldHandleEndpointsWithSpecialCharactersAndNumbers(String input, String expected) {
      String result = UrlUtils.normalizeEndpoint(input);
      assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should handle very long endpoints")
    void shouldHandleVeryLongEndpoints() {
      String longEndpoint = "/api/v1/users/" + "a".repeat(1000) + "/profile";
      String result = UrlUtils.normalizeEndpoint(longEndpoint);
      assertEquals(longEndpoint, result);
    }
  }
}