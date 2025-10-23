/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.mx;

import static com.raymice.swift.utils.IdentifierUtils.getUpdatedFileName;

import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import java.net.URI;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class PACS_008_001_08Route extends DefaultRoute {

  @Override
  public void configure() throws Exception {

    final String inputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getPacs008());
    final URI outputSuccessPath =
        URI.create(
            String.format(
                "file:%s?noop=false", getRoutingConfig().getOutput().getSuccess().getPath()));

    // Take messages from ActiveMQ queue {{app.routing.queue.pacs008}}, validate and route
    // accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .log(
            "📥 Receiving pacs.008.001.08 message from ActiveMQ (queue=${header.JMSDestination}"
                + " uuid=${header.UUID})")
        .onException(Exception.class)
        .log(
            "‼️Error processing message (uuid=${header.UUID}) - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(getErrorEndpoint())
        .handled(true)
        .end()
        .log("Need to implement PACS.008.001.08 specific processing here... (uuid=${header.UUID})")
        .process(
            exchange -> {
              // Set file name based on header
              String fileName = getUpdatedFileName(exchange);
              // Set file name for output
              exchange.getIn().setHeader(Exchange.FILE_NAME, fileName);
            })
        .to(outputSuccessPath.toString())
        .end();
  }
}
