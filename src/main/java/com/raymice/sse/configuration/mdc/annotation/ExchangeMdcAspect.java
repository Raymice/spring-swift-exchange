/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.configuration.mdc.annotation;

import com.raymice.sse.configuration.mdc.MdcService;
import com.raymice.sse.utils.CamelUtils;
import com.raymice.sse.utils.TraceUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.AbstractExchangeEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class ExchangeMdcAspect {

  private final io.micrometer.tracing.Tracer tracer;
  private final MdcService mdcService;

  @Around("@annotation(ExchangeMDC)")
  public Object setMDC(ProceedingJoinPoint joinPoint) throws Throwable {
    // Add MDC fields
    setMdcWithExchange(joinPoint);

    final Object proceed;

    try {
      // Execute method
      proceed = joinPoint.proceed();
    } finally {
      // Clear all MDC fields
      mdcService.clear();
    }

    return proceed;
  }

  private void setMdcWithExchange(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    Exchange camelExchange = null;

    for (Object arg : args) {
      if (arg instanceof Exchange exchange) {
        camelExchange = exchange;
        break;
      } else if (arg instanceof AbstractExchangeEvent camelEvent) {
        camelExchange = camelEvent.getExchange();
        break;
      }
    }

    if (camelExchange != null) {
      final String processId = CamelUtils.getProcessId(camelExchange);
      mdcService.setProcessId(processId);
    }

    final String traceId = TraceUtils.getTraceId(tracer);
    mdcService.setTraceId(traceId);
  }
}
