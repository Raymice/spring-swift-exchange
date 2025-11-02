/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.raymice.swift.utils.FileUtils;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {

  @Test
  void addUuid_ReturnsCorrectFormat_ForValidInputs() {
    String originalFileName = "file.txt";
    UUID uuid = UUID.randomUUID();
    String expected = "$$" + uuid + "$$" + originalFileName;
    String result = FileUtils.addUuid(originalFileName, uuid);

    assertEquals(expected, result);
  }

  @Test
  void addUuid_ThrowsException_ForBlankFileName() {
    UUID uuid = UUID.randomUUID();
    assertThrows(IllegalArgumentException.class, () -> FileUtils.addUuid(" ", uuid));
  }

  @Test
  void addUuid_ThrowsException_ForNullUuid() {
    String originalFileName = "file.txt";
    assertThrows(IllegalArgumentException.class, () -> FileUtils.addUuid(originalFileName, null));
  }

  @Test
  void getUuid_ReturnsUuid_ForValidFileName() {
    UUID uuid = UUID.randomUUID();
    String fileNameWithUuid = "$$" + uuid + "$$file.txt";
    String result = FileUtils.getUuid(fileNameWithUuid);

    assertEquals(uuid.toString(), result);
  }

  @Test
  void getUuid_ThrowsException_ForInvalidFileNameFormat() {
    String invalidFileName = "file.txt";
    assertThrows(RuntimeException.class, () -> FileUtils.getUuid(invalidFileName));
  }

  @Test
  void getUuid_ThrowsException_ForBlankFileName() {
    assertThrows(IllegalArgumentException.class, () -> FileUtils.getUuid(" "));
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
