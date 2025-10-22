/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.input.validation;

import static com.raymice.swift.constant.Global.MX_ID;
import static com.raymice.swift.constant.Global.PACS_008_001_08;
import static com.raymice.swift.utils.ValidatorUtils.isXMLWellFormed;

import com.prowidesoftware.swift.model.MxSwiftMessage;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ValidateRoute extends DefaultRoute {

  @Override
  public void configure() throws Exception {

    final String inputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getInput());
    final String outputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getPacs008());

    // Take messages from ActiveMQ queue {{app.routing.queue.input}}, validate and route accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .log(
            "🔍 Validating well formed message from ActiveMQ (queue=${header.JMSDestination}"
                + " id=${header.JMSMessageID})")
        .onException(Exception.class)
        .log(
            "‼️Error processing message (id=${header.JMSMessageID}) - ${exception.message} -"
                + " ${exception.stacktrace}")
        .to(getErrorEndpoint())
        .handled(true)
        .end()
        .process(
            exchange -> {
              String xml = exchange.getIn().getBody(String.class);

              // Validate XML well-formed
              if (!isXMLWellFormed(xml)) {
                throw new RuntimeException("XML is not well formed");
              }

              // Check message type (MX)
              // Use MxSwiftMessage for performance
              // https://dev.prowidesoftware.com/latest/open-source/iso20022/iso20022-parser/
              MxSwiftMessage msg = MxSwiftMessage.parse(xml);

              // Set MX_ID header
              String id = msg.getMxId().id();
              exchange.getIn().setHeader(MX_ID, StringUtils.isNotBlank(id) ? id : "UNKNOWN");
            })
        .choice()
        .when(header(MX_ID).isEqualTo(PACS_008_001_08))
        .log("✅ Message is an ${header.MX_ID} (id=${header.JMSMessageID})")
        .log("📤 Sending message to PACS.008 queue (id=${header.JMSMessageID})")
        .to(outputQueueUri) // Forward to next queue
        .otherwise()
        .log("❌ Message is not supported type='${header.MX_ID}' (id=${header.JMSMessageID})")
        .to(getErrorEndpoint())
        .endChoice()
        .end();
  }
}
