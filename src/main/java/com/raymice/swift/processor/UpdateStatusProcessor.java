/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.processor;

import static com.raymice.swift.utils.CamelUtils.getProcessId;
import static com.raymice.swift.utils.CamelUtils.getStatus;
import static com.raymice.swift.utils.CamelUtils.setStatus;

import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.exception.WorkflowStatusException;
import com.raymice.swift.routing.DefaultRoute;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Processor to update the status of a process in database and in the Camel exchange headers.
 * Could throw WorkflowStatusException if the status update is not allowed based on the current status (see {@link ErrorProcessor} {@link DefaultRoute#setupCommonExceptionHandling()}).
 * {@link ProcessEntity.Status} enum defines the possible statuses.
 */
@Slf4j
public record UpdateStatusProcessor(ProcessService processService, ProcessEntity.Status newStatus)
    implements Processor {

  @Override
  public void process(Exchange exchange) {
    final String processId = getProcessId(exchange);
    final ProcessEntity.Status actualStatus = getStatus(exchange);

    // Check if status update is allowed
    if (!isAllowToUpdate(actualStatus, newStatus)) {
      log.error(
          "‼️ Status update not allowed for processId={}: current status={}, attempted status={}",
          processId,
          actualStatus,
          newStatus);

      // Set exception in exchange to trigger error handling
      exchange.setException(new WorkflowStatusException("Status update not allowed"));
      return;
    }

    log.debug(
        "Previous status of processId={} was {}, will be upddated to {}",
        processId,
        actualStatus,
        newStatus);

    // Update in database
    processService.updateProcessStatus(Long.parseLong(processId), newStatus);

    // Set in header for further processing
    setStatus(exchange, newStatus);
  }

  private boolean isAllowToUpdate(
      @NotNull ProcessEntity.Status actual, @NotNull ProcessEntity.Status newStatus) {
    if (actual == null || newStatus == null) {
      // Null statuses are not allowed
      return false;
    }

    if (newStatus == ProcessEntity.Status.FAILED) {
      // Always allow updating to 'FAILED'
      return true;
    }

    if (actual == newStatus) {
      // No need to update if the status is the same
      return false;
    }

    if (newStatus == ProcessEntity.Status.CREATED) {
      // Never allow downgrading to 'CREATED'
      return false;
    }

    return actual.ordinal() < newStatus.ordinal();
  }
}
