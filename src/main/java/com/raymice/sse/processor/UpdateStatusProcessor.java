/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.processor;

import static com.raymice.sse.utils.CamelUtils.setStatus;

import com.raymice.sse.db.entity.ProcessEntity;
import com.raymice.sse.db.sevice.ProcessService;
import com.raymice.sse.routing.DefaultRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Processor to update the status of a process in database and in the Camel
 * exchange headers.
 * Could throw WorkflowStatusException if the status update is not allowed based
 * on the current status (see {@link ErrorProcessor}
 * {@link DefaultRoute#setupCommonExceptionHandling()}).
 * {@link ProcessEntity.Status} enum defines the possible statuses.
 */
@Slf4j
public record UpdateStatusProcessor(ProcessService processService, ProcessEntity.Status newStatus)
    implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {

    // Update in database
    processService.updateProcessStatus(exchange, newStatus);

    // Set in header for further processing
    setStatus(exchange, newStatus);
  }
}
