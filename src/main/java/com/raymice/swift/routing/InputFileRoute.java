/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing;

import com.raymice.swift.configuration.RoutingConfig;
import java.net.URI;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InputFileRoute extends RouteBuilder {

  @Autowired RoutingConfig routingConfig;

  @Override
  public void configure() throws Exception {

    final RoutingConfig.Input input = routingConfig.getInput();
    final RoutingConfig.Output output = routingConfig.getOutput();

    // Define the route to consume files from a directory
    // Noop=false => the original file not remains in the source directory after Camel has processed
    // it.
    final URI inputPath = URI.create(String.format("file:%s?noop=false", input.getPath()));
    final URI outputInProgressPath = URI.create(String.format("file:%s", output.getInProgress()));
    final URI outputErrorPath = URI.create(String.format("file:%s", output.getError()));
    final URI outputUnsupportedPath = URI.create(String.format("file:%s", output.getUnsupported()));

    // Global exception handling for the route
    onException(Exception.class)
        .log("‼️Error processing file: ${file:name} - ${exception.message}")
        .to(outputErrorPath.toString())
        .handled(true);

    // Consumes files from input directory
    from(inputPath.toString())
        .routeId(input.getRouteId()) // Assign a unique ID to the route
        .choice()
        .when(header("CamelFileName").endsWith(".xml"))
        .log("⚙️Processing XML file: ${file:name}")
        .to(outputInProgressPath.toString())
        .otherwise()
        .log("🤷‍♂️ Unsupported file extension: ${file:name}")
        .to(outputUnsupportedPath.toString())
        .endChoice()
        .end();

    // Consumes files from inProgress directory
    from(outputInProgressPath.toString())
        .routeId("InProgressToActiveMQ")
        .log("📤 Sending file to ActiveMQ: ${file:name}")
        // TODO REPLACE WITH ACTIVEMQ COMPONENT
        .to(URI.create(String.format("file:%s", output.getSuccess())).toString());
  }

  // Step 1: Validate against XSD schema
  // Step 2: Unmarshal XML to Java Object
  // Step 3 : Process the Java Object as needed
}
