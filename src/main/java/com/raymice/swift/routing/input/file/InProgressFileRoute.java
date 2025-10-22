/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.file;

import static com.raymice.swift.constant.Global.ORIGINAL_FILE_NAME;
import static com.raymice.swift.utils.IdentifierUtils.getOriginalFileName;
import static com.raymice.swift.utils.IdentifierUtils.getUuid;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.constant.Global;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import java.net.URI;
import org.apache.camel.Exchange;
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
            "‼️Error processing file: ${header:ORIGINAL_FILE_NAME} (uuid=${header.UUID}) -"
                + " ${exception.message} - ${exception.stacktrace}")
        .to(getErrorEndpoint())
        .handled(true)
        .end()
        .log("📤 Sending file to ActiveMQ: ${header:ORIGINAL_FILE_NAME} (uuid=${header.UUID})")
        .process(
            exchange -> {
              // Add header to indicate original filename and UUID
              String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
              String originalFileName = getOriginalFileName(fileName);
              String uuid = getUuid(fileName);
              exchange.getIn().setHeader(ORIGINAL_FILE_NAME, originalFileName);
              exchange.getIn().setHeader(Global.UUID, uuid);
            })
        // TODO: setup connection factory
        .to(outputQueueUri)
        .log("✅ File sent to ActiveMQ: ${header.ORIGINAL_FILE_NAME} (uuid=${header.UUID})");
  }
}
