/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration.mdc;

import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class MdcService {

  public static final String MDC_PROCESS_ID = "processId";

  public void setProcessId(@NotBlank String processId) {
    MDC.put(MDC_PROCESS_ID, processId);
  }

  public void clear() {
    MDC.clear();
    ThreadContext.clearAll();
  }
}
