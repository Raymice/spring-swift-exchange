/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift;

import static com.raymice.swift.utils.IdentifierUtils.getUuid;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileMessage;
import org.apache.camel.impl.event.ExchangeCompletedEvent;
import org.apache.camel.impl.event.ExchangeSentEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;

@Slf4j
public class AuditEventNotifier extends EventNotifierSupport {

  @Override
  protected void doStart() throws Exception {
    // Configure which event types to ignore if desired
    setIgnoreCamelContextEvents(true);
    setIgnoreRouteEvents(true);
    setIgnoreServiceEvents(true);
  }

  @Override
  public void notify(CamelEvent event) throws Exception {
    if (event instanceof ExchangeCompletedEvent completedEvent) {
      Exchange exchange = completedEvent.getExchange();
      String routeId = exchange.getFromRouteId();
      String fileName = null;
      String uuid = getUuid(exchange);

      if (exchange.getIn() instanceof GenericFileMessage<?> message) {
        if (message.getGenericFile() != null) {
          fileName = message.getGenericFile().getFileName();
        }
      }

      if (fileName != null) {
        log.info("✅Exchange completed for route: {} (uuid={} file='{}')", routeId, uuid, fileName);
      } else {
        log.info("✅Exchange completed for route: {} (uuid={})", routeId, uuid);
      }

    } else if (event instanceof ExchangeSentEvent sentEvent) {
      Exchange exchange = sentEvent.getExchange();
      String endpointUri = sentEvent.getEndpoint().getEndpointUri();
      long timeTaken = sentEvent.getTimeTaken();
      String uuid = getUuid(exchange);

      log.info("✅ Exchange sent to endpoint: {} in {} ms (uuid={})", endpointUri, timeTaken, uuid);
    }
  }
}
