/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration;

import com.raymice.swift.AuditEventNotifier;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
