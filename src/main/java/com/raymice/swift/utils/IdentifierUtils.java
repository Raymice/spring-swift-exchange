/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import com.raymice.swift.constant.Global;
import java.util.UUID;
import org.apache.camel.Exchange;

public class IdentifierUtils {

  private static final String delimiter = "$";

  public static String addUuid(String originalFileName, UUID uuid) {
    return delimiter + delimiter + uuid + delimiter + delimiter + originalFileName;
  }

  public static String getUuid(String fileNameWithUuid) throws RuntimeException {
    String[] parts = fileNameWithUuid.split("\\" + delimiter + "\\" + delimiter);
    if (parts.length >= 3) {
      return parts[1];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }

  public static String getUuid(Exchange exchange) {
    return exchange.getIn().getHeader(Global.UUID, String.class);
  }

  public static void setUuid(Exchange exchange, String uuid) {
    exchange.getIn().setHeader(Global.UUID, uuid);
  }

  public static String getOriginalFileName(String fileNameWithUuid) throws RuntimeException {
    String[] parts = fileNameWithUuid.split("\\" + delimiter + "\\" + delimiter);
    if (parts.length >= 3) {
      return parts[2];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }

  public static String getOriginalFileName(Exchange exchange) {
    return exchange.getIn().getHeader(Global.ORIGINAL_FILE_NAME, String.class);
  }

  public static void setOriginalFileName(Exchange exchange, String originalFileName) {
    exchange.getIn().setHeader(Global.ORIGINAL_FILE_NAME, originalFileName);
  }
}
