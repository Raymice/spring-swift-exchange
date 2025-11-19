/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@EnableAspectJAutoProxy
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {

  @Valid private Routing routing = new Routing(); // app.routing
  @Valid private Redelivery redelivery = new Redelivery(); // app.redelivery

  @Data
  public static class Routing {
    @Valid private File file = new File(); // app.routing.file
    @Valid private Queue queue = new Queue(); // app.routing.queue
  }

  @Data
  public static class Redelivery {
    @Valid private int maximumRedeliveries = 5; // app.redelivery.maximumRedeliveries
    @Valid private int delay = 500; // app.redelivery.delay
    @Valid private int backOffMultiplier = 2; // app.redelivery.backOffMultiplier
    @Valid private int maximumRedeliveryDelay = 60000; // app.redelivery.maximumRedeliveryDelay
  }

  @Data
  public static class File {
    @Valid private Input input = new Input(); // app.routing.file.input
    @Valid private Output output = new Output(); // app.routing.file.output
  }

  @Data
  public static class Input {
    // Input directory for files
    @NotNull private URI path = URI.create("/tmp/input"); // app.routing.file.input.path
  }

  @Data
  public static class Output {
    // Output directory for success (will be created if not exist)
    @NotNull private URI success = URI.create("/tmp/success"); // app.routing.file.output.success
    // Output directory for errors (will be created if not exist)
    @NotNull private URI error = URI.create("/tmp/error"); // app.routing.file.output.error

    // Output directory for unsupported (will be created if not exist)
    @NotNull
    private URI unsupported = URI.create("/tmp/unsupported"); // app.routing.file.output.unsupported
  }

  @Data
  public static class Queue {
    // Queue for input messages, will validate XML well formedness
    @NotBlank private String validator = "swift-validator"; // app.routing.queue.validator
    // Queue for messages of type pacs.008.001.08
    @NotBlank private String pacs008 = "swift-pacs.008.001.08"; // app.routing.queue.pacs008
    // Dead letter queue for failed messages
    @NotBlank private String deadLetter = "swift-dead-letter"; // app.routing.queue.deadLetter
  }

  public String getFileInputPath() {
    return routing.file.input.path.getPath();
  }

  public String getFileOutputSuccessPath() {
    return routing.file.output.success.getPath();
  }

  public String getFileOutputUnsupportedPath() {
    return routing.file.output.unsupported.getPath();
  }

  public String getFileOutputErrorPath() {
    return routing.file.output.error.getPath();
  }

  public String getQueueValidatorName() {
    return routing.queue.validator;
  }
}
