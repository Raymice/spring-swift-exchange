/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.utils;

import static com.raymice.sse.constant.Common.DELIMITER;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.Validate;

/**
 * Utility class for file.
 */
public class FileUtils {

  /**
   * Extract Process ID from file name with process ID.
   *
   * @param fileNameWithID the file name containing the UUID
   * @return the extracted Process ID as a string
   * @throws RuntimeException error during extraction
   */
  public static String getProcessId(@NotBlank String fileNameWithID) throws RuntimeException {
    Validate.notBlank(fileNameWithID, "FileNameWithID name must not be blank");

    String[] parts = fileNameWithID.split("\\" + DELIMITER + "\\" + DELIMITER);
    if (parts.length >= 3) {
      return parts[1];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithID);
    }
  }

  /**
   * Add processId to original file name.
   *
   * @param originalFileName the original file name
   * @param processId        the process ID to include in the file name
   * @return the new file name with process ID
   */
  public static String addProcessId(@NotBlank String originalFileName, @NotNull Long processId) {
    Validate.notBlank(originalFileName, "OriginalFileName name must not be blank");
    Validate.notNull(processId, "ProcessId name must not be null");

    return DELIMITER + DELIMITER + processId + DELIMITER + DELIMITER + originalFileName;
  }

  /**
   * Extract original file name from file name with UUID.
   *
   * @param fileNameWithUuid the file name containing the UUID
   * @return the extracted original file name
   * @throws RuntimeException error during extraction
   */
  public static String getOriginalFileName(@NotBlank String fileNameWithUuid)
      throws RuntimeException {
    Validate.notBlank(fileNameWithUuid, "FileNameWithUuid name must not be blank");

    String[] parts = fileNameWithUuid.split("\\" + DELIMITER + "\\" + DELIMITER);
    if (parts.length >= 3) {
      return parts[2];
    } else {
      throw new RuntimeException("Invalid file name format: " + fileNameWithUuid);
    }
  }
}
