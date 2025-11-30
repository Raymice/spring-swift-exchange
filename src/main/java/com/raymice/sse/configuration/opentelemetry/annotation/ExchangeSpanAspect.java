/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.configuration.opentelemetry.annotation;

import com.raymice.sse.utils.TraceUtils;
import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Tracer;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class ExchangeSpanAspect {

  private final Tracer tracer;

  @Around("@annotation(ExchangeSpan)")
  public Object createSpan(ProceedingJoinPoint joinPoint) throws Throwable {

    final Object proceed;
    ScopedSpan span = null;
    String name = getName(joinPoint);

    try {
      // Create span
      span = createSpanWithExchange(joinPoint, name);
      // Execute method
      proceed = joinPoint.proceed();
    } catch (Exception ex) {
      if (span != null) {
        // Attach error to the span
        span.error(ex);
      }

      throw ex;
    } finally {
      // Close span
      if (span != null) {
        span.end();
      }
    }

    return proceed;
  }

  private ScopedSpan createSpanWithExchange(ProceedingJoinPoint joinPoint, String name) {
    ScopedSpan span = null;
    Object[] args = joinPoint.getArgs();

    for (Object arg : args) {
      if (arg instanceof Exchange exchange) {
        span = TraceUtils.createSpan(tracer, name, exchange);
        break;
      }
    }
    return span;
  }

  private String getName(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ExchangeSpan exchangeSpan = method.getAnnotation(ExchangeSpan.class);

    if (exchangeSpan != null) {
      return exchangeSpan.name();
    }
    return null;
  }
}
