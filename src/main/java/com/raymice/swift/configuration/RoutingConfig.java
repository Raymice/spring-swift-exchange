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
@ConfigurationProperties(prefix = "app.routing")
public class RoutingConfig {

  @Valid private File file = new File();
  @Valid private Queue queue = new Queue();

  @Data
  public static class File {
    @Valid private Input input = new Input();
    @Valid private Output output = new Output();
  }

  @Data
  public static class Input {
    // Input directory for files
    @NotNull private URI path = URI.create("/tmp/input");
  }

  @Data
  public static class Output {
    // Output directory for success (will be created if not exist)
    @NotNull private URI success = URI.create("/tmp/success");
    // Output directory for errors (will be created if not exist)
    @NotNull private URI error = URI.create("/tmp/error");
    // Output directory for unsupported (will be created if not exist)
    @NotNull private URI unsupported = URI.create("/tmp/unsupported");
  }

  @Data
  public static class Queue {
    // Queue for input messages, will validate XML well formedness
    @NotBlank private String validator = "swift-validator";
    // Queue for messages of type pacs.008.001.08
    @NotBlank private String pacs008 = "swift-pacs.008.001.08";
    // Dead letter queue for failed messages
    @NotBlank private String deadLetter = "swift-dead-letter";
  }
}
