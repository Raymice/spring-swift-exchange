/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.processor;

import static com.raymice.swift.utils.CamelUtils.getProcessId;

import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.exception.WorkflowStatusException;
import com.raymice.swift.utils.StringUtils;
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

  @Override
  public void process(Exchange exchange) throws WorkflowStatusException {
    final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
    final String processId = StringUtils.unknownIfBlank(getProcessId(exchange));

    // Log error message
    log.error("‼️Error processing message: '{}' (processId={})", exception.getMessage(), processId);
    // Update process status to FAILED in database
    new UpdateStatusProcessor(processService, ProcessEntity.Status.FAILED).process(exchange);
  }
}
