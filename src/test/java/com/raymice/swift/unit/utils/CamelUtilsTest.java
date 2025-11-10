/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.raymice.swift.constant.Header;
import com.raymice.swift.utils.CamelUtils;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.Test;

class CamelUtilsTest {

  @Test
  void setProcessId_SetsHeaderCorrectly() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    Long processId = Long.MAX_VALUE;
    CamelUtils.setProcessId(exchange, processId);

    assertEquals(
        String.valueOf(processId), exchange.getIn().getHeader(Header.CUSTOM_HEADER_PROCESS_ID));
  }

  @Test
  void getProcessId_ReturnsHeaderValue() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    Long processId = Long.MAX_VALUE;
    CamelUtils.setProcessId(exchange, processId);
    String result = CamelUtils.getProcessId(exchange);

    assertEquals(String.valueOf(processId), result);
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
