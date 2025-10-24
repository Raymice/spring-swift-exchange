/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.file;

import static com.raymice.swift.utils.IdentifierUtils.addUuid;
import static com.raymice.swift.utils.IdentifierUtils.getFileName;
import static com.raymice.swift.utils.IdentifierUtils.getOriginalFileName;
import static com.raymice.swift.utils.IdentifierUtils.getUuid;
import static com.raymice.swift.utils.IdentifierUtils.setFileName;
import static com.raymice.swift.utils.IdentifierUtils.setOriginalFileName;
import static com.raymice.swift.utils.IdentifierUtils.setUpdatedFileName;
import static com.raymice.swift.utils.IdentifierUtils.setUuid;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import java.net.URI;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InputFileRoute extends DefaultRoute {

  @Override
  public void configure() throws Exception {

    final RoutingConfig.Input input = getRoutingConfig().getInput();
    final RoutingConfig.Output output = getRoutingConfig().getOutput();

    // Define the route to consume files from a directory
    // Noop=false => the original file not remains in the source directory after Camel has processed
    // it.
    final URI inputPath = URI.create(String.format("file:%s?noop=false", input.getPath()));
    final String outputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getInput());
    final String outputUnsupportedPath =
        URI.create(String.format("file:%s", output.getUnsupported())).toString();

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Consumes files from input directory  and sends to ActiveMQ
    from(inputPath.toString())
        .routeId(getRouteId())
        .process(preProcessor)
        .choice()
        .when(header(Exchange.FILE_NAME).endsWith(".xml"))
        .process(successProcessor)
        .to(outputQueueUri)
        .otherwise()
        .process(unsupportedProcessor)
        .to(outputUnsupportedPath)
        .endChoice()
        .end();
  }

  private final org.apache.camel.Processor preProcessor =
      exchange -> {
        final UUID uuid = UUID.randomUUID();
        final String inputPath = exchange.getFromEndpoint().getEndpointUri();
        final String originalFileName = getFileName(exchange);

        log.info("📥 Receiving file '{}' from: {} (uuid={}})", originalFileName, inputPath, uuid);

        // Add UUID to filename to ensure uniqueness
        final String newFileName = addUuid(originalFileName, uuid);
        setFileName(exchange, newFileName);

        // Set custom headers
        setOriginalFileName(exchange, originalFileName);
        setUpdatedFileName(exchange, newFileName);
        setUuid(exchange, uuid.toString());
      };

  private final org.apache.camel.Processor successProcessor =
      exchange -> {
        final String uuid = getUuid(exchange);
        final String fileName = getOriginalFileName(exchange);
        log.info("📤 Sending file to ActiveMQ: '{}' (uuid={})", fileName, uuid);
      };

  private final org.apache.camel.Processor unsupportedProcessor =
      exchange -> {
        final String uuid = getUuid(exchange);
        final String fileName = getOriginalFileName(exchange);
        log.warn("🤷‍♂️ Unsupported file extension: '{}' (uuid={})", fileName, uuid);
      };
}
