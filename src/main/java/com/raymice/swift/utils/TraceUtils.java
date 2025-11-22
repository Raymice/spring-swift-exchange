/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import static com.raymice.swift.utils.CamelUtils.getProcessId;

import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Tracer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.camel.Exchange;

public class TraceUtils {

  public static ScopedSpan createSpan(
      @NotNull Tracer tracer, @NotBlank String spanName, @NotNull Exchange exchange) {
    ScopedSpan span = tracer.startScopedSpan(spanName);
    addProcessId(span, getProcessId(exchange));
    return span;
  }

  public static void addProcessId(@NotNull ScopedSpan span, @NotBlank String processId) {
    span.tag("process.id", processId);
  }
}
