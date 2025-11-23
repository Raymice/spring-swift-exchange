/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.validate;

import static com.raymice.swift.utils.CamelUtils.getMxId;
import static com.raymice.swift.utils.CamelUtils.getProcessId;
import static com.raymice.swift.utils.CamelUtils.getQueueName;
import static com.raymice.swift.utils.CamelUtils.setMxId;
import static com.raymice.swift.utils.XmlUtils.isXMLWellFormed;

import com.prowidesoftware.swift.model.MxSwiftMessage;
import com.raymice.swift.configuration.mdc.MethodWithMdcContext;
import com.raymice.swift.exception.MalformedXmlException;
import com.raymice.swift.exception.UnsupportedException;
import com.raymice.swift.tracing.CustomSpan;
import com.raymice.swift.utils.StringUtils;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {

  @Autowired private Tracer tracer;

  @MethodWithMdcContext
  public void parseAndValidate(Exchange exchange) throws Exception {
    try (CustomSpan topSpan = new CustomSpan(tracer, "parse-and-validate", exchange)) {

      final String processId = getProcessId(exchange);
      final String queueName = getQueueName(exchange);

      log.info(
          "🔍 Validating well formed message from ActiveMQ (processId={}, queue={})",
          processId,
          queueName);

      String xml = exchange.getIn().getBody(String.class);

      // Validate XML well-formed
      boolean isXMLWellFormed;
      try (CustomSpan _ = new CustomSpan(tracer, "xml-formed", exchange)) {
        isXMLWellFormed = isXMLWellFormed(xml);
      }

      if (!isXMLWellFormed) {
        MalformedXmlException ex = new MalformedXmlException();
        topSpan.setError(ex);
        throw ex;
      }

      // Check message type (MX)
      // Use MxSwiftMessage for performance
      // https://dev.prowidesoftware.com/latest/open-source/iso20022/iso20022-parser/
      try (CustomSpan _ = new CustomSpan(tracer, "mx-parse", exchange)) {
        MxSwiftMessage msg = MxSwiftMessage.parse(xml);

        // Set MX_ID header
        String mxId = StringUtils.unknownIfBlank(msg.getMxId().id());
        setMxId(exchange, mxId);
      }
    }
  }

  @MethodWithMdcContext
  public void logProcessor(Exchange exchange) {
    final String processId = getProcessId(exchange);
    final String mxID = getMxId(exchange);

    log.info("✅ Message is an {} (processId={})", mxID, processId);
    log.info("📤 Sending message to PACS.008 queue (processId={})", processId);
  }

  public void unsupportedProcessor(Exchange exchange) throws UnsupportedException {

    final String mxID = getMxId(exchange);
    throw new UnsupportedException(String.format("Message is not a supported type='%s'", mxID));
  }
}
