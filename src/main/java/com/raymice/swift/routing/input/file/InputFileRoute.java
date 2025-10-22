/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.file;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.routing.DefaultRoute;
import java.net.URI;
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
        .choice()
        .when(header("CamelFileName").endsWith(".xml"))
        .log("⚙️Processing XML file: ${file:name}")
        .to(outputInProgressPath.toString())
        .otherwise()
        .log("🤷‍♂️ Unsupported file extension: ${file:name}")
        .to(outputUnsupportedPath.toString())
        .endChoice()
        .end();
  }
}
