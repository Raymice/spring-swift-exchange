/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.raymice.swift.constant.Header;
import com.raymice.swift.utils.CamelUtils;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.Test;

class CamelUtilsTest {

  @Test
  void setUuid_SetsHeaderCorrectly() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String uuid = UUID.randomUUID().toString();
    CamelUtils.setUuid(exchange, uuid);

    assertEquals(uuid, exchange.getIn().getHeader(Header.CUSTOM_HEADER_UUID));
  }

  @Test
  void getUuid_ReturnsHeaderValue() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String uuid = UUID.randomUUID().toString();
    CamelUtils.setUuid(exchange, uuid);
    String result = CamelUtils.getUuid(exchange);

    assertEquals(uuid, result);
  }

  @Test
  void setOriginalFileName_SetsHeaderCorrectly() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String originalFileName = "file.txt";
    CamelUtils.setOriginalFileName(exchange, originalFileName);

    assertEquals(
        originalFileName, exchange.getIn().getHeader(Header.CUSTOM_HEADER_ORIGINAL_FILE_NAME));
  }

  @Test
  void getOriginalFileName_ReturnsHeaderValue() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String originalFileName = "file.txt";
    CamelUtils.setOriginalFileName(exchange, originalFileName);
    String result = CamelUtils.getOriginalFileName(exchange);

    assertEquals(originalFileName, result);
  }
}
