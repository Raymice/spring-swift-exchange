/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.processor;

import static com.raymice.swift.utils.IdentifierUtils.getUuid;

import com.raymice.swift.constant.Global;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErrorLogProcessor implements Processor {

  @Override
  public void process(Exchange exchange) {
    final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
    String uuid = StringUtils.defaultIfBlank(getUuid(exchange), Global.UNKNOWN);

    log.error("‼️Error processing message: '{}' (uuid={})", exception.getMessage(), uuid);
  }
}
