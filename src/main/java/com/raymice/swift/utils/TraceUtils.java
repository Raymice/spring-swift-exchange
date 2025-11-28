/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import static com.raymice.swift.utils.CamelUtils.isCustomHeader;

import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Tracer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.Validate;

public class TraceUtils {

  public static ScopedSpan createSpan(
      @NotNull Tracer tracer, @NotBlank String spanName, @NotNull Exchange exchange) {

    Validate.notNull(tracer, "Tracer must not be null");
    Validate.notBlank(spanName, "Span name must not be blank");
    Validate.notNull(exchange, "Exchange must not be null");

    ScopedSpan span = tracer.startScopedSpan(spanName);

    // Add all the custom headers as tag
    exchange
        .getIn()
        .getHeaders()
        .forEach(
            (key, value) -> {
              if (isCustomHeader(key, value)) {
                key = applyOpenTelemetryConv(key);
                span.tag(key, (String) value);
              }
            });

    return span;
  }

  /**
   * Applies OpenTelemetry naming conventions to a string by converting it to
   * lowercase
   * and replacing underscores with dots.
   *
   * <p>
   * This method follows the OpenTelemetry specification which requires attribute
   * keys
   * to be in lowercase and use dots as separators instead of underscores.
   * </p>
   *
   * @param value the input string to be transformed (must not be blank)
   * @return the transformed string with lowercase letters and dots instead of
   *         underscores
   */
  public static String applyOpenTelemetryConv(@NotBlank String value) {
    Validate.notBlank(value, "Value must not be blank");
    return value.toLowerCase().replaceAll("_", ".");
  }
}
