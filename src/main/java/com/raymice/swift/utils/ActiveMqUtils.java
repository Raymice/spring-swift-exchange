/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class ActiveMqUtils {

  public static String getQueueUri(String queueName) throws IllegalArgumentException {
    if (StringUtils.isBlank(queueName)) {
      throw new IllegalArgumentException("Queue name must not be blank", null);
    }

    return UriComponentsBuilder.fromPath(String.format("activemq:queue:%s", queueName))
        .queryParam("testConnectionOnStartup", "true")
        .build()
        .toUriString();
  }
}
