/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.db.sevice;

import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.repository.ProcessRepo;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {

  private final ProcessRepo processRepo;

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

  public void updateProcessStatus(long processId, ProcessEntity.Status newStatus) {
    processRepo.updateStatusById(newStatus, processId);
    log.info("🔄Process with id={} updated to status: {}", processId, newStatus);
  }
}
