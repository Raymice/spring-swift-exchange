/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.routing.read;

import static com.raymice.sse.utils.CamelUtils.getFileName;
import static com.raymice.sse.utils.CamelUtils.getOriginalFileName;
import static com.raymice.sse.utils.CamelUtils.setFileName;
import static com.raymice.sse.utils.CamelUtils.setOriginalFileName;
import static com.raymice.sse.utils.CamelUtils.setProcessId;
import static com.raymice.sse.utils.CamelUtils.setStatus;
import static com.raymice.sse.utils.CamelUtils.setUpdatedFileName;

import com.raymice.sse.configuration.mdc.annotation.ExchangeMDC;
import com.raymice.sse.configuration.opentelemetry.annotation.ExchangeSpan;
import com.raymice.sse.db.entity.ProcessEntity;
import com.raymice.sse.db.sevice.ProcessService;
import com.raymice.sse.exception.UnsupportedException;
import com.raymice.sse.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FileRouteService {

  private final ProcessService processService;

  /**
   * Processor to handle pre-processing of incoming files
   * - Save in database, renames file, sets headers
   */
  @ExchangeMDC
  @ExchangeSpan(name = "create-process")
  public void createProcess(Exchange exchange) {

    final String inputPath = exchange.getFromEndpoint().getEndpointUri();
    final String originalFileName = getFileName(exchange);
    // Remove all line breaks from file content
    final String fileContent = exchange.getIn().getBody(String.class).replaceAll("\\R", "");

    // Persist process entity in DB
    ProcessEntity process = processService.createProcess(originalFileName, fileContent);
    Long processId = process.getId();

    // Add status in header
    setStatus(exchange, process.getStatus());

    log.info("📥 Receiving file '{}' from: {}", originalFileName, inputPath);

    // Add Process ID to filename to ensure uniqueness
    final String newFileName = FileUtils.addProcessId(originalFileName, processId);
    setFileName(exchange, newFileName);

    // Set custom headers
    setOriginalFileName(exchange, originalFileName);
    setUpdatedFileName(exchange, newFileName);
    setProcessId(exchange, processId);
  }

  /**
   * Processor to log successful processing of supported files
   * - logs file name and Process ID
   */
  @ExchangeMDC
  public void successProcessor(Exchange exchange) {
    final String fileName = getOriginalFileName(exchange);
    log.info("📤 Sending file to ActiveMQ: '{}'", fileName);
  }

  /**
   * Processor to handle unsupported file types
   * - logs warning with file name and Process ID
   */
  @ExchangeMDC
  public void unsupportedProcessor(Exchange exchange) throws UnsupportedException {
    final String fileName = getOriginalFileName(exchange);
    throw new UnsupportedException(
        String.format("🤷‍♂️ Unsupported file extension: '%s'", fileName));
  }
}
