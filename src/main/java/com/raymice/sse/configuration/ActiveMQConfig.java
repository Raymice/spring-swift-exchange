/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.data.activemq")
public class ActiveMQConfig {

  @NotNull private Integer concurrentConsumers; // spring.data.activemq.concurrentConsumers
}
