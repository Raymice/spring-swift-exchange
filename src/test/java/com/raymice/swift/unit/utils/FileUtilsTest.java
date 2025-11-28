/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.raymice.swift.utils.FileUtils;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {

  @Test
  void addProcessId_ReturnsCorrectFormat_ForValidInputs() {
    String originalFileName = "file.txt";
    Long processId = Long.MAX_VALUE;
    String expected = "$$" + processId + "$$" + originalFileName;
    String result = FileUtils.addProcessId(originalFileName, processId);

    assertEquals(expected, result);
  }

  @Test
  void addProcessId_ThrowsException_ForBlankFileName() {
    Long processId = Long.MAX_VALUE;
    assertThrows(IllegalArgumentException.class, () -> FileUtils.addProcessId(" ", processId));
  }

  @Test
  void addProcessId_ThrowsException_ForNullUuid() {
    String originalFileName = "file.txt";
    assertThrows(NullPointerException.class, () -> FileUtils.addProcessId(originalFileName, null));
  }

  @Test
  void getProcessId_ReturnsUuid_ForValidFileName() {
    Long processId = Long.MAX_VALUE;
    String fileNameWithID = "$$" + processId + "$$file.txt";
    String result = FileUtils.getProcessId(fileNameWithID);

    assertEquals(String.valueOf(processId), result);
  }

  @Test
  void getProcessId_ThrowsException_ForInvalidFileNameFormat() {
    String invalidFileName = "file.txt";
    assertThrows(RuntimeException.class, () -> FileUtils.getProcessId(invalidFileName));
  }

  @Test
  void getProcessId_ThrowsException_ForBlankFileName() {
    assertThrows(IllegalArgumentException.class, () -> FileUtils.getProcessId(" "));
  }

  @Test
  void getOriginalFileName_ReturnsFileName_ForValidFileName() {
    UUID uuid = UUID.randomUUID();
    String fileNameWithUuid = "$$" + uuid + "$$file.txt";
    String result = FileUtils.getOriginalFileName(fileNameWithUuid);

    assertEquals("file.txt", result);
  }

  @Test
  void getOriginalFileName_ThrowsException_ForInvalidFileNameFormat() {
    String invalidFileName = "file.txt";

    assertThrows(RuntimeException.class, () -> FileUtils.getOriginalFileName(invalidFileName));
  }

  @Test
  void getOriginalFileName_ThrowsException_ForBlankFileName() {
    assertThrows(IllegalArgumentException.class, () -> FileUtils.getOriginalFileName(" "));
  }
}
