/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.processor;

import com.raymice.sse.utils.CamelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryProcessor implements Processor {

  @Override
  public void process(org.apache.camel.Exchange exchange) throws Exception {
    final String processId = CamelUtils.getProcessId(exchange);

    final Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    final String routeId = exchange.getProperty(Exchange.FAILURE_ROUTE_ID, String.class);
    final int attempt = exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class);
    final int maxAttempts =
        exchange.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER, Integer.class);

    log.warn(
        "🔄 ({}/{}) Retrying operation for processId={} (routeId={} exception={})",
        attempt,
        maxAttempts,
        processId,
        routeId,
        exception.getMessage());
  }
}
