/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.processor;

import com.raymice.sse.configuration.mdc.MdcService;
import com.raymice.sse.configuration.mdc.annotation.ExchangeMDC;
import com.raymice.sse.db.entity.ProcessEntity;
import com.raymice.sse.db.sevice.ProcessService;
import com.raymice.sse.utils.CamelUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Processor to log errors during message processing
 */
@AllArgsConstructor
@Slf4j
@Component
public class ErrorProcessor implements Processor {

  private ProcessService processService;
  private MdcService mdcService;

  @ExchangeMDC
  @Override
  public void process(Exchange exchange) throws Exception {
    final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

    mdcService.setProcessId(CamelUtils.getProcessId(exchange));
    // Log error message
    log.error("‼️Error processing message: '{}'", exception.getMessage());
    // Update process status to FAILED in database
    new UpdateStatusProcessor(processService, ProcessEntity.Status.FAILED).process(exchange);
    mdcService.clear();
  }
}
