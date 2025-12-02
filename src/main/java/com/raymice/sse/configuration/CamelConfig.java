/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.configuration;

import com.raymice.sse.event.AuditEventNotifier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Data;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration class for Apache Camel integration
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.camel")
public class CamelConfig {

  @Autowired AuditEventNotifier auditEventNotifier;
  @Valid private FileProcessor fileProcessor = new FileProcessor(); // spring.camel.fileProcessor

  @Data
  public static class FileProcessor {
    // Auto create the folders if they do not exist
    @NotNull private Boolean autoCreate = true; // spring.camel.fileProcessor.autoCreate

    // The original file not remains in the source directory after Camel has
    // processed it.
    @NotNull private Boolean noop = false; // spring.camel.fileProcessor.noop

    // Looking at the subdirectories or not (use false to avoid conflits)
    @NotNull private Boolean recursive = false; // spring.camel.fileProcessor.recursive

    // Move files in folder while processing
    @NotBlank
    private String preMoveFolder = ".inprogress"; // spring.camel.fileProcessor.preMoveFolder

    // Include hidden files or not (use false to avoid conflits)
    @NotNull private Boolean hiddenFiles = false; // spring.camel.fileProcessor.hiddenFiles

    // Include hidden directories or not (use false to avoid conflits)
    @NotNull private Boolean hiddenDirs = false; // spring.camel.fileProcessor.hiddenDirs

    // Character set to use when reading files
    @NotBlank private String charset = "UTF-8"; // spring.camel.fileProcessor.charset

    // Interval to check for read lock in milliseconds
    @NotNull
    private Integer readLockCheckInterval = 500; // spring.camel.fileProcessor.readLockCheckInterval

    // Duration to wait for read lock in milliseconds
    // readLockTimeout need to be at least readLockCheckInterval * 2
    @NotNull private Integer readLockTimeout = 10000; // spring.camel.fileProcessor.readLockTimeout
  }

  @Bean
  public CamelContextConfiguration camelContextConfiguration() {
    return new CamelContextConfiguration() {
      @Override
      public void beforeApplicationStart(CamelContext camelContext) {
        camelContext.getManagementStrategy().addEventNotifier(auditEventNotifier);
      }

      @Override
      public void afterApplicationStart(CamelContext camelContext) {
        // Optional: perform actions after CamelContext has started
      }
    };
  }

  /**
   * Defines a virtual thread pool for Camel routes
   *
   * @return the virtual thread pool executor service
   */
  @Bean(destroyMethod = "shutdown", name = "camelVirtualThreadPool")
  public ExecutorService virtualThreadPool() {
    // Use virtual threads for lightweight concurrency (Java 21)
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
