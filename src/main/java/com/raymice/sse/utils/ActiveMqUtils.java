/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.utils;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.Validate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility class for ActiveMQ operations.
 */
public class ActiveMqUtils {

  /**
   * Constructs the ActiveMQ queue URI with specified parameters.
   *
   * @param queueName the name of the queue
   * @return the constructed queue URI
   */
  public static String getQueueUri(@NotBlank String queueName, int concurrentConsumers) {

    Validate.notBlank(queueName, "Queue name must not be blank");

    UriComponentsBuilder builder =
        UriComponentsBuilder.fromPath(String.format("activemq:queue:%s", queueName))
            .queryParam("testConnectionOnStartup", "true");

    if (concurrentConsumers > 0) {
      builder = builder.queryParam("concurrentConsumers", concurrentConsumers);
    }

    return builder.build().toUriString();
  }
}
