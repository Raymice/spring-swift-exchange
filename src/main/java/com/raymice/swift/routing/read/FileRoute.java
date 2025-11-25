/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.read;

import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
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

  private final SpringRedisIdempotentRepository myRedisIdempotentRepository;
  private final FileRouteService fileRouteService;

  public FileRoute(
      SpringRedisIdempotentRepository myRedisIdempotentRepository,
      FileRouteService fileRouteService) {
    this.myRedisIdempotentRepository = myRedisIdempotentRepository;
    this.fileRouteService = fileRouteService;
  }

  @Autowired
  @Qualifier("camelVirtualThreadPool")
  private ExecutorService virtualThreadPool;

  @Override
  public void configure() {

    // Define the route to consume files from a directory
    final String inputPath = buildFileUri();

    final String outputQueueUri =
        ActiveMqUtils.getQueueUri(getApplicationConfig().getQueueValidatorName());

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Route for consuming files from the inbox directory
    from(inputPath)
        .routeId(getRouteId())
        .threads()
        .executorService(virtualThreadPool)
        .process(fileRouteService::createProcess)
        .choice()
        .when(header(Exchange.FILE_NAME).endsWith(".xml"))
        .process(fileRouteService::successProcessor)
        .to(outputQueueUri)
        .otherwise()
        .process(fileRouteService::unsupportedProcessor)
        .endChoice()
        .end();
  }

  /**
   * Builds the file URI for consuming files from a directory with various configuration parameters.
   * The URI includes settings for auto-creation of directories, file processing behavior,
   * hidden file handling, and read locking mechanisms to prevent duplicate processing.
   *
   * @return A string representing the configured file URI with all necessary parameters
   * @see <a href="https://camel.apache.org/components/4.14.x/file-component.html">https://camel.apache.org</a>
   */
  private String buildFileUri() {
    // TODO externalize read lock parameters
    return UriComponentsBuilder.fromPath(
            String.format("file:%s", getApplicationConfig().getFileInputPath()))
        // Auto create the folder
        .queryParam("autoCreate", "true")
        // The original file not remains in the source directory after Camel has processed it.
        .queryParam("noop", "false")
        // Not looking at the subdirectories to avoid conflicts
        .queryParam("recursive", "false")
        // Move files in hidden "inprogress" folder
        .queryParam("preMove", ".inprogress")
        // Do not include hidden files
        .queryParam("includeHiddenFiles", "false")
        // Do not include hidden directories
        .queryParam("includeHiddenDirs", "false")
        // Only input
        .queryParam("exchangePattern", "InOnly")
        // Specify charset
        .queryParam("charset", "UTF-8")
        // This combines the idempotent and changed strategies, providing a
        // robust read lock that leverages both change detection and an idempotent repository
        // for clustered scenarios
        .queryParam("readLock", "idempotent-changed")
        // Use a shared Redis-based Idempotent Repository for read lock to prevent multiple
        // instances processing the same file
        .queryParam("idempotentRepository", "#myRedisIdempotentRepository")
        .queryParam("readLockCheckInterval", "500")
        // readLockTimeout need to be at least readLockCheckInterval * 2
        .queryParam("readLockTimeout", "10000")
        .build()
        .toUriString();
  }
}
