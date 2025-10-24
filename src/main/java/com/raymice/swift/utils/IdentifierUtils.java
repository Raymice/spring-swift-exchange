/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import com.raymice.swift.constant.Global;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

public class IdentifierUtils {

  private static final String delimiter = "$";

  public static String addUuid(String originalFileName, UUID uuid) throws IllegalArgumentException {
    if (StringUtils.isBlank(originalFileName)) {
      throw new IllegalArgumentException("OriginalFileName name must not be blank", null);
    }
    if (uuid == null) {
      throw new IllegalArgumentException("Uuid name must not be null", null);
    }

    return delimiter + delimiter + uuid + delimiter + delimiter + originalFileName;
  }

  public static String getUuid(String fileNameWithUuid) throws RuntimeException {
    if (StringUtils.isBlank(fileNameWithUuid)) {
      throw new IllegalArgumentException("FileNameWithUuid name must not be blank", null);
    }

    String[] parts = fileNameWithUuid.split("\\" + delimiter + "\\" + delimiter);
    if (parts.length >= 3) {
      return parts[1];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }

  public static String getUuid(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Global.UUID);
  }

  public static void setUuid(Exchange exchange, String uuid) throws IllegalArgumentException {
    setHeader(exchange, Global.UUID, uuid);
  }

  public static String getOriginalFileName(String fileNameWithUuid) throws RuntimeException {
    if (StringUtils.isBlank(fileNameWithUuid)) {
      throw new IllegalArgumentException("FileNameWithUuid name must not be blank", null);
    }

    String[] parts = fileNameWithUuid.split("\\" + delimiter + "\\" + delimiter);
    if (parts.length >= 3) {
      return parts[2];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }

  public static String getOriginalFileName(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Global.ORIGINAL_FILE_NAME);
  }

  public static String getUpdatedFileName(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Global.UPDATED_FILE_NAME);
  }

  public static void setOriginalFileName(Exchange exchange, String originalFileName)
      throws IllegalArgumentException {
    setHeader(exchange, Global.ORIGINAL_FILE_NAME, originalFileName);
  }

  public static void setUpdatedFileName(Exchange exchange, String updatedFileName)
      throws IllegalArgumentException {
    setHeader(exchange, Global.UPDATED_FILE_NAME, updatedFileName);
  }

  public static String getFileName(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Exchange.FILE_NAME);
  }

  public static void setFileName(Exchange exchange, String fileName)
      throws IllegalArgumentException {
    setHeader(exchange, Exchange.FILE_NAME, fileName);
  }

  private static void setHeader(Exchange exchange, String headerName, String headerValue)
      throws IllegalArgumentException {
    if (exchange == null) {
      throw new IllegalArgumentException("Exchange must not be null", null);
    }
    if (StringUtils.isBlank(headerName)) {
      throw new IllegalArgumentException("HeaderName name must not be blank", null);
    }
    if (StringUtils.isBlank(headerValue)) {
      throw new IllegalArgumentException("HeaderValue name must not be blank", null);
    }

    exchange.getIn().setHeader(headerName, headerValue);
  }

  private static String getHeader(Exchange exchange, String headerName)
      throws IllegalArgumentException {
    if (exchange == null) {
      throw new IllegalArgumentException("Exchange must not be null", null);
    }
    if (StringUtils.isBlank(headerName)) {
      throw new IllegalArgumentException("HeaderName name must not be blank", null);
    }

    return exchange.getIn().getHeader(headerName, String.class);
  }
}
