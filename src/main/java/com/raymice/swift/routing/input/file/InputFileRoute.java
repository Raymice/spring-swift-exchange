/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.file;

import static com.raymice.swift.constant.Global.ORIGINAL_FILE_NAME;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.constant.Global;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.IdentifierUtils;
import java.net.URI;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

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
    final URI outputInProgressPath =
        URI.create(String.format("file:%s?noop=false", output.getInProgress()));
    final URI outputUnsupportedPath = URI.create(String.format("file:%s", output.getUnsupported()));

    // Consumes files from input directory
    from(inputPath.toString())
        .routeId(getRouteId()) // Assign a unique ID to the route
        .onException(Exception.class)
        .log(
            "‼️Error processing file: ${file:name} - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(getErrorEndpoint())
        .handled(true)
        .end()
        .log(String.format("📥 Receiving file '${file:name}' from: %s", inputPath))
        .choice()
        .when(header("CamelFileName").endsWith(".xml"))
        .log("⚙️Processing XML file: '${file:name}'")
        .process(
            exchange -> {
              UUID uuid = UUID.randomUUID();
              // Add UUID to filename to ensure uniqueness
              String originalFileName =
                  exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
              String newFileName = IdentifierUtils.addUuid(originalFileName, uuid);
              exchange.getIn().setHeader(Exchange.FILE_NAME, newFileName);
              exchange.getIn().setHeader(ORIGINAL_FILE_NAME, originalFileName);
              exchange.getIn().setHeader(Global.UUID, uuid);
            })
        .log(
            String.format(
                "📤 Sending file '${header.ORIGINAL_FILE_NAME}' (uuid=${header.UUID}) to %s",
                outputInProgressPath))
        .to(outputInProgressPath.toString())
        .otherwise()
        .log("🤷‍♂️ Unsupported file extension: ${file:name}")
        .to(outputUnsupportedPath.toString())
        .endChoice()
        .end();
  }
}
