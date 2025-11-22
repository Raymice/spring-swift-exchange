/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.raymice.swift.utils.TraceUtils;
import org.junit.jupiter.api.Test;

public class TraceUtilsTest {

  @Test
  void applyOpenTelemetryConv_shouldConvertToLowerCaseAndReplaceUnderscores() {
    // Test with mixed case and underscores
    String input = "HEADER_NAME_WITH_UNDERSCORES";
    String expected = "header.name.with.underscores";
    assertEquals(expected, TraceUtils.applyOpenTelemetryConv(input));
  }

  @Test
  void applyOpenTelemetryConv_shouldHandleAlreadyLowercaseAndDots() {
    // Test with already lowercase and dots
    String input = "header.name.with.dots";
    String expected = "header.name.with.dots";
    assertEquals(expected, TraceUtils.applyOpenTelemetryConv(input));
  }
}
