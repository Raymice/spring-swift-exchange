/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.raymice.swift.utils.TraceUtils;
import io.micrometer.tracing.Tracer;
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

  @Test
  void createSpan_shouldThrowNullPointerExceptionWhenTracerIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> {
          TraceUtils.createSpan(null, "spanName", null);
        });
  }

  @Test
  void createSpan_shouldThrowNullPointerExceptionWhenExchangeIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> {
          TraceUtils.createSpan(Tracer.NOOP, "spanName", null);
        });
  }

  @Test
  void createSpan_shouldThrowIllegalArgumentExceptionWhenSpanNameIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          TraceUtils.createSpan(Tracer.NOOP, " ", null);
        });
  }
}
