/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing;

import static com.raymice.swift.constant.Global.MX_ID;
import static com.raymice.swift.constant.Global.PACS_008_001_08;

import com.prowidesoftware.swift.model.MxSwiftMessage;
import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.utils.ActiveMqUtils;
import java.net.URI;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindTypeRoute extends RouteBuilder {

  @Autowired RoutingConfig routingConfig;

  @Override
  public void configure() throws Exception {

    final RoutingConfig.Output output = routingConfig.getOutput();
    final URI outputErrorPath = URI.create(String.format("file:%s", output.getError()));
    final String inputQueueUri = ActiveMqUtils.getQueueUri(routingConfig.getQueue().getFindType());
    final String outputQueueUri = ActiveMqUtils.getQueueUri(routingConfig.getQueue().getPacs008());

    // Global exception handling for the route
    onException(Exception.class)
        .log(
            LoggingLevel.ERROR,
            "‼️Error processing file: ${file:name} - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(outputErrorPath.toString())
        .handled(true);

    from(inputQueueUri)
        .routeId("FindTypeActiveMQRoute")
        .log(
            "Find type message from ActiveMQ (queue=${header.JMSDestination}"
                + " id=${header.JMSMessageID})")
        .process(
            exchange -> {
              String xml = exchange.getIn().getBody(String.class);
              // Use MxSwiftMessage for performance
              MxSwiftMessage msg = MxSwiftMessage.parse(xml);

              // Set MX_ID header
              String id = msg.getMxId().id();
              exchange.getIn().setHeader(MX_ID, StringUtils.isNotBlank(id) ? id : "UNKNOWN");
            })
        .choice()
        .when(header(MX_ID).isEqualTo(PACS_008_001_08))
        .log("✅ Message is an ${header.MX_ID} (id=${header.JMSMessageID})")
        .to(outputQueueUri) // Forward to next queue
        .otherwise()
        .log("❌ Message is not supported type='${header.MX_ID}' (id=${header.JMSMessageID})")
        .to(outputErrorPath.toString())
        .endChoice()
        .end();
  }
}
