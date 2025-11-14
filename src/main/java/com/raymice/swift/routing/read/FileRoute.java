/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.read;

import static com.raymice.swift.utils.CamelUtils.getFileName;
import static com.raymice.swift.utils.CamelUtils.getOriginalFileName;
import static com.raymice.swift.utils.CamelUtils.getProcessId;
import static com.raymice.swift.utils.CamelUtils.setFileName;
import static com.raymice.swift.utils.CamelUtils.setOriginalFileName;
import static com.raymice.swift.utils.CamelUtils.setProcessId;
import static com.raymice.swift.utils.CamelUtils.setStatus;
import static com.raymice.swift.utils.CamelUtils.setUpdatedFileName;

import com.raymice.swift.configuration.ApplicationConfig;
import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.exception.UnsupportedException;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import com.raymice.swift.utils.FileUtils;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Route to read files from a directory and process them
 */
@Slf4j
@Component
public class FileRoute extends DefaultRoute {

  @Autowired private SpringRedisIdempotentRepository myRedisIdempotentRepository;

  @Autowired
  @Qualifier("camelVirtualThreadPool")
  private ExecutorService virtualThreadPool;

  @Autowired private ProcessService processService;

  @Override
  public void configure() {

    final ApplicationConfig.Routing routing = getApplicationConfig().getRouting();
    final ApplicationConfig.Input input = routing.getFile().getInput();

    // Define the route to consume files from a directory
    // TODO externalize read lock parameters
    final String inputPath =
        UriComponentsBuilder.fromPath(String.format("file:%s", input.getPath()))
            // The original file not remains in the source directory after Camel has processed it.
            .queryParam("noop", "false")
            // This combines the idempotent and changed strategies, providing a
            // robust read lock that leverages both change detection and an idempotent repository
            // for clustered scenarios
            .queryParam("readLock", "idempotent-changed")
            // Use a shared Redis-based Idempotent Repository for read lock to prevent multiple
            // instances processing the same file
            .queryParam("idempotentRepository", "#myRedisIdempotentRepository")
            .queryParam("readLockCheckInterval", "1")
            .queryParam("readLockTimeout", "200")
            .build()
            .toUriString();

    final String outputQueueUri = ActiveMqUtils.getQueueUri(routing.getQueue().getValidator());

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Route for consuming files from the inbox directory
    from(inputPath)
        .routeId(getRouteId())
        .threads()
        .executorService(virtualThreadPool)
        .process(createProcess)
        .choice()
        .when(header(Exchange.FILE_NAME).endsWith(".xml"))
        .process(successProcessor)
        .to(outputQueueUri)
        .otherwise()
        .process(unsupportedProcessor)
        .endChoice()
        .end();
  }

  /**
   * Processor to handle pre-processing of incoming files
   * - Save in database, renames file, sets headers
   */
  private final org.apache.camel.Processor createProcess =
      exchange -> {
        final String inputPath = exchange.getFromEndpoint().getEndpointUri();
        final String originalFileName = getFileName(exchange);
        // Remove all line breaks from file content
        final String fileContent = exchange.getIn().getBody(String.class).replaceAll("\\R", "");

        // Persist process entity in DB
        ProcessEntity process = processService.createProcess(originalFileName, fileContent);
        Long processId = process.getId();

        // Add status in header
        setStatus(exchange, process.getStatus());

        log.info(
            "📥 Receiving file '{}' from: {} (processId={}})",
            originalFileName,
            inputPath,
            processId);

        // Add Process ID to filename to ensure uniqueness
        final String newFileName = FileUtils.addProcessId(originalFileName, processId);
        setFileName(exchange, newFileName);

        // Set custom headers
        setOriginalFileName(exchange, originalFileName);
        setUpdatedFileName(exchange, newFileName);
        setProcessId(exchange, processId);
      };

  /**
   * Processor to log successful processing of supported files
   * - logs file name and Process ID
   */
  private final org.apache.camel.Processor successProcessor =
      exchange -> {
        final String processId = getProcessId(exchange);
        final String fileName = getOriginalFileName(exchange);
        log.info("📤 Sending file to ActiveMQ: '{}' (processId={})", fileName, processId);
      };

  /**
   * Processor to handle unsupported file types
   * - logs warning with file name and Process ID
   */
  private final org.apache.camel.Processor unsupportedProcessor =
      exchange -> {
        final String fileName = getOriginalFileName(exchange);
        throw new UnsupportedException(
            String.format("🤷‍♂️ Unsupported file extension: '%s'", fileName));
      };
}
