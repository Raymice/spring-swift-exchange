/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.process.swift.mx;

import static com.raymice.swift.utils.CamelUtils.getQueueName;
import static com.raymice.swift.utils.CamelUtils.getUpdatedFileName;
import static com.raymice.swift.utils.CamelUtils.setFileName;

import com.raymice.swift.configuration.mdc.annotation.ExchangeMDC;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PACS_008_001_08RouteService {

  @ExchangeMDC
  public void logProcessor(Exchange exchange) {

    final String queueName = getQueueName(exchange);
    log.info("📥 Receiving pacs.008.001.08 message from ActiveMQ (queue={})", queueName);
    log.warn("Need to implement PACS.008.001.08 specific processing here...");
  }

  public void setNameProcessor(Exchange exchange) {
    // Set file name based on header
    String fileName = getUpdatedFileName(exchange);
    // Set file name for output
    setFileName(exchange, fileName);
  }
}
