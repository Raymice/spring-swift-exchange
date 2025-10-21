/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.utils.ActiveMqUtils;
import java.net.URI;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InProgressFileRoute extends RouteBuilder {

  @Autowired RoutingConfig routingConfig;

  @Override
  public void configure() throws Exception {

    final RoutingConfig.Output output = routingConfig.getOutput();
    final URI outputInProgressPath =
        URI.create(String.format("file:%s?noop=false", output.getInProgress()));
    final URI outputErrorPath = URI.create(String.format("file:%s", output.getError()));

    final String outputQueueUri = ActiveMqUtils.getQueueUri(routingConfig.getQueue().getInput());

    // Global exception handling for the route
    onException(Exception.class)
        .log(
            "‼️Error processing file: ${file:name} - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(outputErrorPath.toString())
        .handled(true);

    // Consumes files from inProgress directory and sends to ActiveMQ
    from(outputInProgressPath.toString())
        .routeId("InProgressToActiveMQ")
        .log("📤 Sending file to ActiveMQ: ${file:name}")
        // TODO: setup connection factory
        .to(outputQueueUri)
        .log("✅ File sent to ActiveMQ: ${file:name}");
  }
}
