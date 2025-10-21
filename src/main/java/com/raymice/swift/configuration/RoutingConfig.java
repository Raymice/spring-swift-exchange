/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.routing")
public class RoutingConfig {

  @Valid private Input input;
  @Valid private Output output;
  @Valid private Queue queue;

  @Data
  public static class Input {

    // Input directory for files
    @NotNull private URI path;

    // File extension to include
    @NotBlank(message = "Input include cannot be blank!")
    private String include;

    // Unique id of the route
    @NotBlank(message = "Input routeId cannot be blank!")
    private String routeId;
  }

  @Data
  public static class Output {
    // Output directory for in progress (will be created if not exist)
    @NotNull private URI inProgress;
    // Output directory for success (will be created if not exist)
    @NotNull private URI success;
    // Output directory for errors (will be created if not exist)
    @NotNull private URI error;
    // Output directory for unsupported (will be created if not exist)
    @NotNull private URI unsupported;
  }

  @Data
  public static class Queue {
    // Queue for input messages, will validate XML well formedness
    @NotBlank private String input;
    // Queue for messages to find the type
    @NotBlank private String findType;
    // Queue for messages of type pacs.008.001.08
    @NotBlank private String pacs008;
  }

  @PostConstruct
  void postConstruct() {
    System.out.println("Input: " + input.toString());
    System.out.println("Output: " + output.toString());
    System.out.println("Queue: " + queue.toString());
  }
}
