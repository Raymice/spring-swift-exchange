/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.raymice.swift.utils.IdentifierUtils;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.Test;

class IdentifierUtilsTest {

  @Test
  void addUuid_ReturnsCorrectFormat_ForValidInputs() {
    String originalFileName = "file.txt";
    UUID uuid = UUID.randomUUID();
    String expected = "$$" + uuid + "$$" + originalFileName;
    String result = IdentifierUtils.addUuid(originalFileName, uuid);

    assertEquals(expected, result);
  }

  @Test
  void addUuid_ThrowsException_ForBlankFileName() {
    UUID uuid = UUID.randomUUID();
    assertThrows(IllegalArgumentException.class, () -> IdentifierUtils.addUuid(" ", uuid));
  }

  @Test
  void addUuid_ThrowsException_ForNullUuid() {
    String originalFileName = "file.txt";
    assertThrows(
        IllegalArgumentException.class, () -> IdentifierUtils.addUuid(originalFileName, null));
  }

  @Test
  void getUuid_ReturnsUuid_ForValidFileName() {
    UUID uuid = UUID.randomUUID();
    String fileNameWithUuid = "$$" + uuid + "$$file.txt";
    String result = IdentifierUtils.getUuid(fileNameWithUuid);

    assertEquals(uuid.toString(), result);
  }

  @Test
  void getUuid_ThrowsException_ForInvalidFileNameFormat() {
    String invalidFileName = "file.txt";
    assertThrows(RuntimeException.class, () -> IdentifierUtils.getUuid(invalidFileName));
  }

  @Test
  void getUuid_ThrowsException_ForBlankFileName() {
    assertThrows(IllegalArgumentException.class, () -> IdentifierUtils.getUuid(" "));
  }

  @Test
  void getOriginalFileName_ReturnsFileName_ForValidFileName() {
    UUID uuid = UUID.randomUUID();
    String fileNameWithUuid = "$$" + uuid + "$$file.txt";
    String result = IdentifierUtils.getOriginalFileName(fileNameWithUuid);

    assertEquals("file.txt", result);
  }

  @Test
  void getOriginalFileName_ThrowsException_ForInvalidFileNameFormat() {
    String invalidFileName = "file.txt";

    assertThrows(
        RuntimeException.class, () -> IdentifierUtils.getOriginalFileName(invalidFileName));
  }

  @Test
  void getOriginalFileName_ThrowsException_ForBlankFileName() {
    assertThrows(IllegalArgumentException.class, () -> IdentifierUtils.getOriginalFileName(" "));
  }

  @Test
  void setUuid_SetsHeaderCorrectly() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String uuid = UUID.randomUUID().toString();
    IdentifierUtils.setUuid(exchange, uuid);

    assertEquals(uuid, exchange.getIn().getHeader("UUID"));
  }

  @Test
  void getUuid_ReturnsHeaderValue() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String uuid = UUID.randomUUID().toString();
    exchange.getIn().setHeader("UUID", uuid);
    String result = IdentifierUtils.getUuid(exchange);

    assertEquals(uuid, result);
  }

  @Test
  void setOriginalFileName_SetsHeaderCorrectly() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String originalFileName = "file.txt";
    IdentifierUtils.setOriginalFileName(exchange, originalFileName);

    assertEquals(originalFileName, exchange.getIn().getHeader("ORIGINAL_FILE_NAME"));
  }

  @Test
  void getOriginalFileName_ReturnsHeaderValue() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setIn(new DefaultMessage(exchange));
    String originalFileName = "file.txt";
    exchange.getIn().setHeader("ORIGINAL_FILE_NAME", originalFileName);
    String result = IdentifierUtils.getOriginalFileName(exchange);

    assertEquals(originalFileName, result);
  }
}
