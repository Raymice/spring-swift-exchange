/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.configuration;

import com.raymice.sse.event.AuditEventNotifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Apache Camel integration
 */
@Configuration
public class CamelConfig {

  @Autowired AuditEventNotifier auditEventNotifier;

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
