/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing;

import static com.raymice.swift.constant.Global.IS_WELL_FORMED;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.utils.ActiveMqUtils;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
public class ValidateRoute extends RouteBuilder {

  @Autowired RoutingConfig routingConfig;

  @Override
  public void configure() throws Exception {

    final RoutingConfig.Output output = routingConfig.getOutput();
    final URI outputErrorPath = URI.create(String.format("file:%s", output.getError()));
    final String inputQueueUri = ActiveMqUtils.getQueueUri(routingConfig.getQueue().getInput());
    final String outputQueueUri = ActiveMqUtils.getQueueUri(routingConfig.getQueue().getFindType());

    // Global exception handling for the route
    onException(Exception.class)
        .log(
            "‼️Error processing file: ${file:name} - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(outputErrorPath.toString())
        .handled(true);

    // Take messages from ActiveMQ queue {{app.routing.queue.input}}, validate and route accordingly
    from(inputQueueUri)
        .routeId("ValidateActiveMQRoute")
        .log(
            "🔍 Validating well formed message from ActiveMQ (queue=${header.JMSDestination}"
                + " id=${header.JMSMessageID})")
        .process(
            exchange -> {
              String body = exchange.getIn().getBody(String.class);

              if (body != null && isXMLWellFormed(body)) {
                exchange.getIn().setHeader(IS_WELL_FORMED, true);
              } else {
                exchange.getIn().setHeader(IS_WELL_FORMED, false);
              }
            })
        .choice()
        .when(header(IS_WELL_FORMED).isEqualTo(true))
        .log("✅ Message is well formed (id=${header.JMSMessageID})")
        .to(outputQueueUri) // Forward to next queue
        .otherwise()
        .log("❌ Message is not well formed")
        .to(outputErrorPath.toString())
        .endChoice()
        .end();
  }

  public static boolean isXMLWellFormed(String xmlstring) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.parse(
          new InputSource(new StringReader(xmlstring))); // Parsing will fail if not well-formed
      return true;
    } catch (SAXException | IOException e) {
      System.err.println("XML is not well-formed: " + e.getMessage());
      return false;
    } catch (Exception e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
      return false;
    }
  }
}
