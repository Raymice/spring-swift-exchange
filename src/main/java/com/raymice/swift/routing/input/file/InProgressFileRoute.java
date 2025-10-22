/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.file;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class InProgressFileRoute extends DefaultRoute {

  @Override
  public void configure() throws Exception {

    final RoutingConfig.Output output = getRoutingConfig().getOutput();
    final URI outputInProgressPath =
        URI.create(String.format("file:%s?noop=false", output.getInProgress()));
    final String outputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getInput());

    // Consumes files from inProgress directory and sends to ActiveMQ
    from(outputInProgressPath.toString())
        .routeId(getRouteId())
        .onException(Exception.class)
        .log(
            "‼️Error processing file: ${file:name} - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(getErrorEndpoint())
        .handled(true)
        .end()
        .log("📤 Sending file to ActiveMQ: ${file:name}")
        // TODO: setup connection factory
        .to(outputQueueUri)
        .log("✅ File sent to ActiveMQ: ${file:name}");
  }
}
