/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.processor;

import com.raymice.sse.configuration.mdc.annotation.ExchangeMDC;
import com.raymice.sse.db.entity.ProcessEntity;
import com.raymice.sse.db.sevice.ProcessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Processor to handle unsupported operations
 */
@AllArgsConstructor
@Slf4j
@Component
public class UnsupportedProcessor implements Processor {

  private ProcessService processService;

  @ExchangeMDC
  @Override
  public void process(Exchange exchange) throws Exception {
    final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

    // Log unsupported operation warning
    log.warn("⚠️ Unsupported operation: {}", exception.getMessage());

    // Update process status to unsupported in database
    new UpdateStatusProcessor(processService, ProcessEntity.Status.UNSUPPORTED).process(exchange);
  }
}
