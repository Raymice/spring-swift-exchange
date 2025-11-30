/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.raymice.sse.utils.ActiveMqUtils;
import org.junit.jupiter.api.Test;

class ActiveMqUtilsTest {

  @Test
  void getQueueUri_ReturnsCorrectUri_ForValidQueueName() {
    String queueName = "orders";
    String expectedUri = "activemq:queue:orders?testConnectionOnStartup=true&concurrentConsumers=5";

    String result = ActiveMqUtils.getQueueUri(queueName);
    assertEquals(expectedUri, result);
  }

  @Test
  void getQueueUri_ThrowsException_ForBlankQueueName() {
    String queueName = " ";
    assertThrows(IllegalArgumentException.class, () -> ActiveMqUtils.getQueueUri(queueName));
  }

  @Test
  void getQueueUri_ReturnsCorrectUri_ForQueueNameWithSpecialCharacters() {
    String queueName = "order#123";
    String expectedUri =
        "activemq:queue:order#123?testConnectionOnStartup=true&concurrentConsumers=5";

    String result = ActiveMqUtils.getQueueUri(queueName);

    assertEquals(expectedUri, result);
  }

  @Test
  void getQueueUri_ThrowsException_ForNullQueueName() {
    String queueName = null;
    assertThrows(NullPointerException.class, () -> ActiveMqUtils.getQueueUri(queueName));
  }

  @Test
  void getQueueUri_ReturnsCorrectUri_ForQueueNameWithSpaces() {
    String queueName = "order queue";
    String expectedUri =
        "activemq:queue:order queue?testConnectionOnStartup=true&concurrentConsumers=5";

    String result = ActiveMqUtils.getQueueUri(queueName);
    assertEquals(expectedUri, result);
  }

  @Test
  void getQueueUri_ReturnsCorrectUri_ForQueueNameWithUnicodeCharacters() {
    String queueName = "订单";
    String expectedUri = "activemq:queue:订单?testConnectionOnStartup=true&concurrentConsumers=5";

    String result = ActiveMqUtils.getQueueUri(queueName);
    assertEquals(expectedUri, result);
  }
}
