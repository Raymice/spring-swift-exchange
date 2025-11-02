/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import static com.raymice.swift.constant.Common.DELIMITER;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for file.
 */
public class FileUtils {

  /**
   * Extract UUID from file name with UUID.
   * @param fileNameWithUuid the file name containing the UUID
   * @return the extracted UUID as a string
   * @throws RuntimeException error during extraction
   */
  public static String getUuid(String fileNameWithUuid) throws RuntimeException {
    if (StringUtils.isBlank(fileNameWithUuid)) {
      throw new IllegalArgumentException("FileNameWithUuid name must not be blank", null);
    }

    String[] parts = fileNameWithUuid.split("\\" + DELIMITER + "\\" + DELIMITER);
    if (parts.length >= 3) {
      return parts[1];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }

  /**
   * Add UUID to original file name.
   * @param originalFileName the original file name
   * @param uuid the UUID to add
   * @return the new file name with UUID
   * @throws IllegalArgumentException error during addition
   */
  public static String addUuid(String originalFileName, UUID uuid) throws IllegalArgumentException {
    if (StringUtils.isBlank(originalFileName)) {
      throw new IllegalArgumentException("OriginalFileName name must not be blank", null);
    }
    if (uuid == null) {
      throw new IllegalArgumentException("Uuid name must not be null", null);
    }

    return DELIMITER + DELIMITER + uuid + DELIMITER + DELIMITER + originalFileName;
  }

  /**
   * Extract original file name from file name with UUID.
   * @param fileNameWithUuid the file name containing the UUID
   * @return the extracted original file name
   * @throws RuntimeException error during extraction
   */
  public static String getOriginalFileName(String fileNameWithUuid) throws RuntimeException {
    if (StringUtils.isBlank(fileNameWithUuid)) {
      throw new IllegalArgumentException("FileNameWithUuid name must not be blank", null);
    }

    String[] parts = fileNameWithUuid.split("\\" + DELIMITER + "\\" + DELIMITER);
    if (parts.length >= 3) {
      return parts[2];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }
}
