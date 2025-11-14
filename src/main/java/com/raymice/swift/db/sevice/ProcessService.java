/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.db.sevice;

import com.raymice.swift.configuration.profile.TestProfileOnly;
import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.repository.ProcessRepo;
import com.raymice.swift.exception.WorkflowStatusException;
import com.raymice.swift.utils.CamelUtils;
import jakarta.validation.constraints.NotNull;
import java.rmi.UnexpectedException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {

  private final ProcessRepo processRepo;

  /**
   * Create a new process record in the database
   * @param name file name
   * @param payload file content
   * @return the saved ProcessEntity
   */
  public ProcessEntity createProcess(String name, String payload) {
    ProcessEntity process = new ProcessEntity();
    process.setName(name);
    process.setPayload(payload);
    process.setStatus(ProcessEntity.Status.CREATED);
    process.setCreatedAt(LocalDateTime.now());
    process.setUpdatedAt(LocalDateTime.now());

    ProcessEntity savedProcess = processRepo.save(process);
    log.info("💾Process with id={} saved successfully (file='{}')", savedProcess.getId(), name);
    return savedProcess;
  }

  /**
   * Find a process by its id
   * @param processId the id of the process to find
   * @return the found ProcessEntity
   * @throws IllegalArgumentException if the process is not found
   */
  public ProcessEntity findById(long processId) {
    return processRepo
        .findById(processId)
        .orElseThrow(
            () ->
                new IllegalArgumentException("Process with id=%d not found".formatted(processId)));
  }

  /**
   * Update the status of a process in the database
   * @param exchange Camel Exchange
   * @param newStatus the new status to set
   */
  @Transactional
  public void updateProcessStatus(Exchange exchange, ProcessEntity.Status newStatus)
      throws WorkflowStatusException, UnexpectedException {

    final String processId = CamelUtils.getProcessId(exchange);
    final ProcessEntity.Status actualStatus = CamelUtils.getStatus(exchange);

    if (actualStatus == null) {
      throw new UnexpectedException("No status on exchange");
    }

    // Check if status update is allowed
    if (!isStatusAllowedToUpdate(actualStatus, newStatus)) {
      log.error(
          "‼️ Status update not allowed for processId={}: current status={}, attempted status={}",
          processId,
          actualStatus,
          newStatus);

      throw new WorkflowStatusException("Status update not allowed");
    }

    log.debug(
        "Previous status of processId={} was {}, will be upddated to {}",
        processId,
        actualStatus,
        newStatus);

    processRepo.updateStatusById(newStatus, Long.parseLong(processId));
    log.info(
        "🔄Process with id={} updated from status={} to status={}",
        processId,
        actualStatus,
        newStatus);
  }

  @TestProfileOnly
  public List<ProcessEntity> findAll() {
    return processRepo.findAll();
  }

  @TestProfileOnly
  public void deleteAll() {
    processRepo.deleteAll();
  }

  private boolean isStatusAllowedToUpdate(
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
