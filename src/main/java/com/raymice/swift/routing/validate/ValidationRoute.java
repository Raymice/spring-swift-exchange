/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.validate;

import static com.raymice.swift.constant.Mx.PACS_008_001_08;
import static com.raymice.swift.utils.CamelUtils.getMxId;
import static com.raymice.swift.utils.CamelUtils.getProcessId;
import static com.raymice.swift.utils.CamelUtils.getQueueName;
import static com.raymice.swift.utils.CamelUtils.setMxId;
import static com.raymice.swift.utils.XmlUtils.isXMLWellFormed;

import com.prowidesoftware.swift.model.MxSwiftMessage;
import com.raymice.swift.configuration.ApplicationConfig;
import com.raymice.swift.constant.Header;
import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.exception.MalformedXmlException;
import com.raymice.swift.exception.UnsupportedException;
import com.raymice.swift.processor.UpdateStatusProcessor;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import com.raymice.swift.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ValidationRoute extends DefaultRoute {

  @Override
  public void configure() {
    final ApplicationConfig.Routing routing = getApplicationConfig().getRouting();

    final String inputQueueUri = ActiveMqUtils.getQueueUri(routing.getQueue().getValidator());
    final String outputQueueUri = ActiveMqUtils.getQueueUri(routing.getQueue().getPacs008());

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Take messages from ActiveMQ queue {{app.routing.queue.input}}, validate and route accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .transacted()
        .process(parsingProcessor)
        .choice()
        .when(header(Header.CUSTOM_HEADER_MX_ID).isEqualTo(PACS_008_001_08))
        .process(logProcessor)
        .process(new UpdateStatusProcessor(getProcessService(), ProcessEntity.Status.VALIDATED))
        .to(outputQueueUri) // Forward to next queue
        .otherwise()
        .process(unsupportedProcessor)
        .endChoice()
        .end();
  }

  private final org.apache.camel.Processor unsupportedProcessor =
      exchange -> {
        final String mxID = getMxId(exchange);
        throw new UnsupportedException(String.format("Message is not a supported type='%s'", mxID));
      };

  private final org.apache.camel.Processor logProcessor =
      exchange -> {
        final String processId = getProcessId(exchange);
        final String mxID = getMxId(exchange);
        log.info("✅ Message is an {} (processId={})", mxID, processId);
        log.info("📤 Sending message to PACS.008 queue (processId={})", processId);
      };

  private final org.apache.camel.Processor parsingProcessor =
      exchange -> {
        final String processId = getProcessId(exchange);
        final String queueName = getQueueName(exchange);
        log.info(
            "🔍 Validating well formed message from ActiveMQ (processId={}, queue={})",
            processId,
            queueName);

        String xml = exchange.getIn().getBody(String.class);

        // Validate XML well-formed
        if (!isXMLWellFormed(xml)) {
          throw new MalformedXmlException();
        }

        // Check message type (MX)
        // Use MxSwiftMessage for performance
        // https://dev.prowidesoftware.com/latest/open-source/iso20022/iso20022-parser/
        MxSwiftMessage msg = MxSwiftMessage.parse(xml);

        // Set MX_ID header
        String mxId = StringUtils.unknownIfBlank(msg.getMxId().id());
        setMxId(exchange, mxId);
      };
}
