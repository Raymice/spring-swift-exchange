/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ActiveMqUtils {

  public static String getQueueUri(String queueName) {
    return UriComponentsBuilder.fromPath(String.format("activemq:queue:%s", queueName))
        .queryParam("testConnectionOnStartup", "true")
        .build()
        .toUriString();
  }
}
