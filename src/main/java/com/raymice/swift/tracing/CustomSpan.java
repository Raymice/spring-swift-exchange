/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.tracing;

import com.raymice.swift.utils.TraceUtils;
import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Tracer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.Exchange;

@Data
@NoArgsConstructor(force = true)
public class CustomSpan implements AutoCloseable {

  private final ScopedSpan span;

  public CustomSpan(Tracer tracer, String name) {
    span = tracer.startScopedSpan(name);
  }

  public CustomSpan(Tracer tracer, String name, Exchange exchange) {
    span = TraceUtils.createSpan(tracer, name, exchange);
  }

  public void setError(Exception ex) {
    span.error(ex);
  }

  @Override
  public void close() throws Exception {
    this.span.end();
  }
}
