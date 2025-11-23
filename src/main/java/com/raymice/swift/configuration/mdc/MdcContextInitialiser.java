/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration.mdc;

import com.raymice.swift.utils.CamelUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.ExchangeCompletedEvent;
import org.apache.camel.impl.event.ExchangeCreatedEvent;
import org.apache.camel.impl.event.ExchangeSendingEvent;
import org.apache.camel.impl.event.ExchangeSentEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class MdcContextInitialiser {

  private final MdcService mdcService;

  @Around("@annotation(MethodWithMdcContext)")
  public Object setMDC(ProceedingJoinPoint joinPoint) throws Throwable {
    // Add MDC fields
    setMdcContextForMethod(joinPoint);

    // Execute method
    Object proceed = joinPoint.proceed();

    // Clear all MDC fields
    mdcService.clear();
    return proceed;
  }

  private void setMdcContextForMethod(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();

    for (Object arg : args) {
      if (arg instanceof Exchange exchange) {
        final String processId = CamelUtils.getProcessId(exchange);

        mdcService.setProcessId(processId);
        break;

      } else if (arg instanceof CamelEvent camelEvent) {
        String processId = null;

        switch (camelEvent) {
          case ExchangeCreatedEvent createdEvent ->
              processId = CamelUtils.getProcessId(createdEvent.getExchange());
          case ExchangeSendingEvent sendingEvent ->
              processId = CamelUtils.getProcessId(sendingEvent.getExchange());
          case ExchangeSentEvent sentEvent ->
              processId = CamelUtils.getProcessId(sentEvent.getExchange());
          case ExchangeCompletedEvent completedEvent ->
              processId = CamelUtils.getProcessId(completedEvent.getExchange());
          default -> {}
        }

        if (StringUtils.isNotBlank(processId)) {
          mdcService.setProcessId(processId);
        }
        break;
      }
    }
  }
}
