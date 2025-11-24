/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration.mdc.annotation;

import com.raymice.swift.configuration.mdc.MdcService;
import com.raymice.swift.utils.CamelUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.AbstractExchangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class ExchangeMdcAspect {

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
      if (StringUtils.isNotBlank(processId)) {
        mdcService.setProcessId(processId);
      }
    }
  }
}
