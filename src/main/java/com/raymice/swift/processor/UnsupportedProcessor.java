/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.processor;

import static com.raymice.swift.utils.CamelUtils.getProcessId;

import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.utils.StringUtils;
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

  @Override
  public void process(Exchange exchange) {
    final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
    final String processId = StringUtils.unknownIfBlank(getProcessId(exchange));

    // Log unsupported operation warning
    log.warn("⚠️Unsupported operation: {} (processId={})", exception.getMessage(), processId);

    // Update process status to unsupported in database
    new UpdateStatusProcessor(processService, ProcessEntity.Status.UNSUPPORTED).process(exchange);
  }
}
