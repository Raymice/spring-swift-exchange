/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import java.util.UUID;

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

  public static String getOriginalFileName(String fileNameWithUuid) throws RuntimeException {
    String[] parts = fileNameWithUuid.split("\\" + delimiter + "\\" + delimiter);
    if (parts.length >= 3) {
      return parts[2];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }
}
