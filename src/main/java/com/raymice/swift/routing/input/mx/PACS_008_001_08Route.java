/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.mx;

import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import org.springframework.stereotype.Component;

@Component
public class PACS_008_001_08Route extends DefaultRoute {

  @Override
  public void configure() throws Exception {

    final String inputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getPacs008());

    // Take messages from ActiveMQ queue {{app.routing.queue.pacs008}}, validate and route
    // accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .log(
            "📥 Receiving pacs.008.001.08 message from ActiveMQ (queue=${header.JMSDestination}"
                + " id=${header.JMSMessageID})")
        .onException(Exception.class)
        .log(
            "‼️Error processing message (id=${header.JMSMessageID}) - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(getErrorEndpoint())
        .handled(true)
        .end()
        .log("Need to implement PACS.008.001.08 specific processing here...")
        .end();
  }
}
