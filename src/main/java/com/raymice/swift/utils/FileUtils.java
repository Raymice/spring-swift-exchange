/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import static com.raymice.swift.constant.Common.DELIMITER;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for file.
 */
public class FileUtils {

  /**
   * Extract Process ID from file name with process ID.
   * @param fileNameWithID the file name containing the UUID
   * @return the extracted Process ID as a string
   * @throws RuntimeException error during extraction
   */
  public static String getProcessId(String fileNameWithID) throws RuntimeException {
    if (StringUtils.isBlank(fileNameWithID)) {
      throw new IllegalArgumentException("fileNameWithID name must not be blank", null);
    }

    String[] parts = fileNameWithID.split("\\" + DELIMITER + "\\" + DELIMITER);
    if (parts.length >= 3) {
      return parts[1];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithID);
    }
  }

  /**
   * Add processId to original file name.
   * @param originalFileName the original file name
   * @param processId the process ID to include in the file name
   * @return the new file name with process ID
   * @throws IllegalArgumentException error during addition
   */
  public static String addProcessId(String originalFileName, Long processId)
      throws IllegalArgumentException {
    if (StringUtils.isBlank(originalFileName)) {
      throw new IllegalArgumentException("OriginalFileName name must not be blank", null);
    }
    if (processId == null) {
      throw new IllegalArgumentException("ProcessId name must not be null", null);
    }

    return DELIMITER + DELIMITER + processId + DELIMITER + DELIMITER + originalFileName;
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
