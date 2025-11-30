/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.routing.validate;

import static com.raymice.sse.utils.CamelUtils.getMxId;
import static com.raymice.sse.utils.CamelUtils.getQueueName;
import static com.raymice.sse.utils.CamelUtils.setMxId;
import static com.raymice.sse.utils.XmlUtils.isXMLWellFormed;

import com.prowidesoftware.swift.model.MxId;
import com.prowidesoftware.swift.model.mx.MxParseUtils;
import com.raymice.sse.configuration.mdc.annotation.ExchangeMDC;
import com.raymice.sse.configuration.opentelemetry.annotation.ExchangeSpan;
import com.raymice.sse.exception.MalformedXmlException;
import com.raymice.sse.exception.UnsupportedException;
import com.raymice.sse.tracing.CustomSpan;
import com.raymice.sse.utils.StringUtils;
import io.micrometer.tracing.Tracer;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ValidationRouteService {

  private final Tracer tracer;

  @ExchangeMDC
  @ExchangeSpan(name = "parse-and-validate")
  public void parseAndValidate(Exchange exchange) throws Exception {

    final String queueName = getQueueName(exchange);

    log.info("🔍 Validating well formed message from ActiveMQ (queue={})", queueName);

    String xml = exchange.getIn().getBody(String.class);

    // Validate XML well-formed
    boolean isXMLWellFormed;
    try (CustomSpan _ = new CustomSpan(tracer, "xml-formed", exchange)) {
      isXMLWellFormed = isXMLWellFormed(xml);
    }

    if (!isXMLWellFormed) {
      throw new MalformedXmlException();
    }

    // Check message type (MX)
    try (CustomSpan _ = new CustomSpan(tracer, "mx-parse", exchange)) {
      MxId id = extractMxId(xml);
      // Set MX_ID header
      setMxId(exchange, StringUtils.unknownIfBlank(id.id()));
    }
  }

  @ExchangeMDC
  public void logProcessor(Exchange exchange) {
    final String mxID = getMxId(exchange);
    log.info("✅ Message is an {}", mxID);
    log.info("📤 Sending message to PACS.008 queue");
  }

  public void unsupportedProcessor(Exchange exchange) throws UnsupportedException {
    final String mxID = getMxId(exchange);
    throw new UnsupportedException(String.format("Message is not a supported type='%s'", mxID));
  }

  /**
   * Extracts the MX ID from the given XML string.
   *
   * <p>
   * This method parses the XML document to identify the message type and version
   * according to the ISO 20022 standard. It uses the MxParseUtils utility class
   * to detect the message identifier which typically follows the pattern
   * "pacs.002.001.10" in the XML namespace declaration.
   * </p>
   *
   * <p>
   * Example XML structure being parsed:
   *
   * <pre>{@code
   * <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
   * }</pre>
   * </p>
   *
   * @param xml the XML string to parse for the MX ID
   * @return the detected MxId object, or null if no identifier could be
   *         determined
   */
  private MxId extractMxId(String xml) {
    Optional<MxId> detectedIdentifier = MxParseUtils.identifyMessage(xml);
    return detectedIdentifier.orElse(null);
  }
}
