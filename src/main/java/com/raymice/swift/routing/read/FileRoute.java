/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.read;

import static com.raymice.swift.utils.CamelUtils.getFileName;
import static com.raymice.swift.utils.CamelUtils.getOriginalFileName;
import static com.raymice.swift.utils.CamelUtils.getUuid;
import static com.raymice.swift.utils.CamelUtils.setFileName;
import static com.raymice.swift.utils.CamelUtils.setOriginalFileName;
import static com.raymice.swift.utils.CamelUtils.setUpdatedFileName;
import static com.raymice.swift.utils.CamelUtils.setUuid;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import com.raymice.swift.utils.FileUtils;
import java.net.URI;
import java.util.UUID;
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

  @Override
  public void configure() {

    final RoutingConfig.Input input = getRoutingConfig().getInput();
    final RoutingConfig.Output output = getRoutingConfig().getOutput();

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
            .queryParam("readLockCheckInterval", "10")
            .queryParam("readLockTimeout", "200")
            .build()
            .toUriString();

    final String outputQueueUri =
        ActiveMqUtils.getQueueUri(getRoutingConfig().getQueue().getInput());
    final String outputUnsupportedPath =
        URI.create(String.format("file:%s", output.getUnsupported())).toString();

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Route for consuming files from the inbox directory
    from(inputPath)
        .routeId(getRouteId())
        .threads()
        .executorService(virtualThreadPool)
        .process(preProcessor)
        .choice()
        .when(header(Exchange.FILE_NAME).endsWith(".xml"))
        .process(successProcessor)
        .to(outputQueueUri)
        .otherwise()
        .process(unsupportedProcessor)
        .to(outputUnsupportedPath)
        .endChoice()
        .end();
  }

  /**
   * Processor to handle pre-processing of incoming files
   * - generates UUID, renames file, sets headers
   */
  private final org.apache.camel.Processor preProcessor =
      exchange -> {
        final UUID uuid = UUID.randomUUID();
        final String inputPath = exchange.getFromEndpoint().getEndpointUri();
        final String originalFileName = getFileName(exchange);

        log.info("📥 Receiving file '{}' from: {} (uuid={}})", originalFileName, inputPath, uuid);

        // Add UUID to filename to ensure uniqueness
        final String newFileName = FileUtils.addUuid(originalFileName, uuid);
        setFileName(exchange, newFileName);

        // Set custom headers
        setOriginalFileName(exchange, originalFileName);
        setUpdatedFileName(exchange, newFileName);
        setUuid(exchange, uuid.toString());
      };

  /**
   * Processor to log successful processing of supported files
   * - logs file name and UUID
   */
  private final org.apache.camel.Processor successProcessor =
      exchange -> {
        final String uuid = getUuid(exchange);
        final String fileName = getOriginalFileName(exchange);
        log.info("📤 Sending file to ActiveMQ: '{}' (uuid={})", fileName, uuid);
      };

  /**
   * Processor to handle unsupported file types
   * - logs warning with file name and UUID
   */
  private final org.apache.camel.Processor unsupportedProcessor =
      exchange -> {
        final String uuid = getUuid(exchange);
        final String fileName = getOriginalFileName(exchange);
        log.warn("🤷‍♂️ Unsupported file extension: '{}' (uuid={})", fileName, uuid);
      };
}
