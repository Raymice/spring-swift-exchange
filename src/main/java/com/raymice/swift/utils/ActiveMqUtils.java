/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility class for ActiveMQ operations.
 */
public class ActiveMqUtils {

  /**
   * Constructs the ActiveMQ queue URI with specified parameters.
   * @param queueName the name of the queue
   * @return the constructed queue URI
   * @throws IllegalArgumentException if the queue name is blank
   */
  public static String getQueueUri(String queueName) throws IllegalArgumentException {
    if (StringUtils.isBlank(queueName)) {
      throw new IllegalArgumentException("Queue name must not be blank", null);
    }

    return UriComponentsBuilder.fromPath(String.format("activemq:queue:%s", queueName))
        .queryParam("testConnectionOnStartup", "true")
        // TODO add external configuration for concurrentConsumers
        .queryParam("concurrentConsumers", "5")
        .build()
        .toUriString();
  }
}
