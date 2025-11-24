/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Utility class for testing purposes.
 */
@Slf4j
public class TestingUtils {

  /**
   * Counts the number of files in a directory and its subdirectories.
   *
   * @param directoryPath the path to the directory to count files in
   * @return the number of files in the directory and its subdirectories
   */
  public static int countFilesInDirectory(String directoryPath) {
    return FileUtils.listFiles(
            new File(directoryPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        .size();
  }

  /**
   * Copies a file from the source path to the destination path.
   *
   * @param sourcePath      the path of the source file to be copied
   * @param destinationPath the path where the file will be copied to
   * @throws IOException if an I/O error occurs during the copy operation
   */
  public static void copyFile(String sourcePath, String destinationPath) throws IOException {
    FileUtils.copyFile(new File(sourcePath), new File(destinationPath));
  }

  /**
   * Cleans the specified directories by deleting all their contents.
   *
   * @param paths The paths of the directories to be cleaned.
   * @throws IOException If an I/O error occurs while cleaning the directories.
   */
  public static void cleanDirectories(String... paths) throws IOException {
    for (String path : paths) {
      // Clean only if directory exists
      File f = new File(path);
      if (f.isDirectory() && f.exists()) {
        log.info("🧹Cleaning up {}", f.getPath());
        FileUtils.cleanDirectory(f);
      }
    }
  }

  /**
   * Retrieves the first file found in the specified directory.
   *
   * @param directoryPath the path to the directory to search for files
   * @return an Optional containing the first File found in the directory, or an empty Optional if no files are found
   */
  public static Optional<File> getFileInDirectory(String directoryPath) {
    return FileUtils.listFiles(
            new File(directoryPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        .stream()
        .findFirst();
  }

  /**
   * Checks if a directory contains a specific number of files within a timeout period.
   *
   * @param directoryPath The path of the directory to check.
   * @param expectedCount The expected number of files in the directory.
   * @return true if the directory contains the expected number of files within the timeout period,
   *         false otherwise.
   * @throws InterruptedException If the thread is interrupted while waiting for the files.
   */
  public static boolean hasFileInDirectory(String directoryPath, int expectedCount)
      throws InterruptedException {
    LocalDateTime startTime = LocalDateTime.now();
    while (countFilesInDirectory(directoryPath) != expectedCount) {
      if (Duration.between(startTime, LocalDateTime.now()).toSeconds() > expectedCount * 1.5) {
        log.error("⏱ Timeout waiting for file in directory: {}", directoryPath);
        return false;
      }

      // Wait to prevent high CPU usage
      Thread.sleep(10);
    }

    Optional<File> resultFileOpt = getFileInDirectory(directoryPath);
    if (resultFileOpt.isEmpty()) {
      throw new AssertionError("Output file not found in expected directory");
    } else {
      log.info("✅Output file found: {}", resultFileOpt.get().getAbsolutePath());
      return true;
    }
  }
}
