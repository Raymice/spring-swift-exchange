/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration.mdc;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MdcService {

  public static final String MDC_PROCESS_ID = "processId";
  public static final String MDC_TRACE_ID = "traceId";

  public void setProcessId(@Nullable String processId) {
    if (StringUtils.isNotBlank(processId)) {
      MDC.put(MDC_PROCESS_ID, processId);
    }
  }

  public void setTraceId(@Nullable String traceId) {
    if (StringUtils.isNotBlank(traceId)) {
      MDC.put(MDC_TRACE_ID, traceId);
    }
  }

  public void clear() {
    MDC.clear();
    ThreadContext.clearAll();
  }
}
