/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration;

import com.raymice.swift.event.AuditEventNotifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Apache Camel integration
 */
@Configuration
public class CamelConfig {

  @Bean
  public CamelContextConfiguration camelContextConfiguration() {
    return new CamelContextConfiguration() {
      @Override
      public void beforeApplicationStart(CamelContext camelContext) {
        camelContext.getManagementStrategy().addEventNotifier(new AuditEventNotifier());
      }

      @Override
      public void afterApplicationStart(CamelContext camelContext) {
        // Optional: perform actions after CamelContext has started
      }
    };
  }

  /**
   * Defines a virtual thread pool for Camel routes
   * @return the virtual thread pool executor service
   */
  @Bean(destroyMethod = "shutdown", name = "camelVirtualThreadPool")
  public ExecutorService virtualThreadPool() {
    // Use virtual threads for lightweight concurrency (Java 21)
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
