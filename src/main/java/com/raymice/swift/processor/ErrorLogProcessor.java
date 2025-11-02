/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.processor;

import static com.raymice.swift.utils.CamelUtils.getUuid;

import com.raymice.swift.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Processor to log errors during message processing
 */
@Slf4j
@Component
public class ErrorLogProcessor implements Processor {

  @Override
  public void process(Exchange exchange) {
    final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
    String uuid = StringUtils.unknownIfBlank(getUuid(exchange));

    log.error("‼️Error processing message: '{}' (uuid={})", exception.getMessage(), uuid);
  }
}
