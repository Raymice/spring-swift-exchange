/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.workflow;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.exception.MalformedXmlException;
import com.raymice.swift.integration.Containers;
import com.raymice.swift.routing.read.FileRoute;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
@SpringBootTest()
@ActiveProfiles("test")
public class End2EndTest {

  @Autowired private RoutingConfig routingConfig;
  @Autowired private CamelContext camelContext;
  @Autowired private ProcessService processService;
  @Container private static final Containers containers = new Containers();

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    containers.applyDynamicProperties(registry);
  }

  @BeforeEach
  void beforeEach() throws Exception {
    // Clean up input and output directories before each test
    log.info("🧹Cleaning up input and output directories before test");
    cleanDirectories(
        routingConfig.getInput().getPath().toString(),
        routingConfig.getOutput().getUnsupported().getPath(),
        routingConfig.getOutput().getSuccess().getPath(),
        routingConfig.getOutput().getError().getPath());

    log.info("Cleaning DB before test");
    processService.deleteAll();
  }

  @Test
  void unsupportedExtension_movesFileToUnsupportedDirectory()
      throws IOException, InterruptedException {

    final String inputWorkflowPath = routingConfig.getInput().getPath().toString();
    final String outputUnsupportedPath = routingConfig.getOutput().getUnsupported().getPath();
    final String inputFileName = "unsupported.json";
    final String testFilePath = "src/test/resources/%s".formatted(inputFileName);
    final String inputFilePath = "%s/%s".formatted(inputWorkflowPath, inputFileName);

    // Copy of the input file to the input directory will trigger the Camel route
    copyFile(testFilePath, inputFilePath);

    // Assert that the file is moved to the unsupported directory (end of processing)
    assertTrue(hasFileInDirectory(outputUnsupportedPath, 1));

    // Assert the process status is set to UNSUPPORTED in database
    assertStatusInDatabase(ProcessEntity.Status.UNSUPPORTED, 1);
  }

  @Test
  @ExtendWith(OutputCaptureExtension.class)
  void malformedXML_movesFileToErrorDirectory(CapturedOutput output)
      throws IOException, InterruptedException {

    final String inputWorkflowPath = routingConfig.getInput().getPath().toString();
    final String outputErrorPath = routingConfig.getOutput().getError().getPath();
    final String inputFileName = "malformed.xml";
    final String testFilePath = "src/test/resources/%s".formatted(inputFileName);
    final String inputFilePath = "%s/%s".formatted(inputWorkflowPath, inputFileName);

    // Copy of the input file to the input directory will trigger the Camel route
    copyFile(testFilePath, inputFilePath);

    // Assert that the file is moved to the error directory (end of processing)
    assertTrue(hasFileInDirectory(outputErrorPath, 1));

    // Verify that MalformedXmlException was throw during processing
    assertThat(output.getOut()).contains(new MalformedXmlException().getMessage());

    // Assert the process status is set to FAILED in database
    assertStatusInDatabase(ProcessEntity.Status.FAILED, 1);
  }

  @Test
  void processesPacs00800108_and_placesFileInSuccessDirectory() throws Exception {

    final String inputWorkflowPath = routingConfig.getInput().getPath().toString();
    final String outputSuccessPath = routingConfig.getOutput().getSuccess().getPath();
    final String inputFileName = "pacs.008.001.08.xml";
    final String testFilePath = "src/test/resources/mx/%s".formatted(inputFileName);
    final String inputFilePath = "%s/%s".formatted(inputWorkflowPath, inputFileName);

    // Copy of the input file to the input directory will trigger the Camel route
    copyFile(testFilePath, inputFilePath);

    // Assert that the file is moved to the success directory (end of processing)
    assertTrue(hasFileInDirectory(outputSuccessPath, 1));

    // Assert the process status is set to COMPLETED in database
    assertStatusInDatabase(ProcessEntity.Status.COMPLETED, 1);
  }

  @Test
  void processBatchOfFiles_movesAllFilesToSuccessDirectory() throws Exception {
    final String inputWorkflowPath = routingConfig.getInput().getPath().toString();
    final String outputSuccessPath = routingConfig.getOutput().getSuccess().getPath();

    // Stop the route to prepare for batch processing
    camelContext.getRouteController().stopRoute(FileRoute.class.getSimpleName());

    final String testFileName = "pacs.008.001.08.xml";
    final int iterations = 100;
    final Duration expectedDuration = Duration.of(8, java.time.temporal.ChronoUnit.SECONDS);

    for (int i = 0; i < iterations; i++) {
      String testFilePath = "src/test/resources/mx/%s".formatted(testFileName);
      String inputFilePath = "%s/%d-%s".formatted(inputWorkflowPath, i, testFileName);
      copyFile(testFilePath, inputFilePath);
    }

    // Start the route to process the batch of files
    camelContext.getRouteController().startRoute(FileRoute.class.getSimpleName());
    final LocalDateTime now = LocalDateTime.now();

    // Wait to find all files in the success directory (end of processing)
    assertTrue(hasFileInDirectory(outputSuccessPath, iterations));

    final Duration totalDuration = Duration.between(now, LocalDateTime.now());

    log.info("⏱ Total time to process {} files: {} seconds", iterations, totalDuration.toSeconds());

    assertTrue(
        totalDuration.compareTo(expectedDuration) < 0,
        "Processing time exceeded expected threshold");

    // Assert the process status is set to COMPLETED in database
    assertStatusInDatabase(ProcessEntity.Status.COMPLETED, iterations);
  }

  private void assertStatusInDatabase(ProcessEntity.Status expectedStatus, int expectedCount) {
    List<ProcessEntity> processEntities = processService.findAll();
    assertEquals(expectedCount, processEntities.size());
    for (ProcessEntity entity : processEntities) {
      assertEquals(expectedStatus, entity.getStatus());
    }
  }

  private int countFilesInDirectory(String directoryPath) {
    return FileUtils.listFiles(
            new File(directoryPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        .size();
  }

  private void copyFile(String sourcePath, String destinationPath) throws IOException {
    FileUtils.copyFile(new File(sourcePath), new File(destinationPath));
  }

  private void cleanDirectories(String... paths) throws IOException {
    for (String path : paths) {
      // Clean only if directory exists
      File f = new File(path);
      if (f.isDirectory() && f.exists()) {
        FileUtils.cleanDirectory(f);
      }
    }
  }

  private Optional<File> getFileInDirectory(String directoryPath) {
    return FileUtils.listFiles(
            new File(directoryPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        .stream()
        .findFirst();
  }

  private boolean hasFileInDirectory(String directoryPath, int expectedCount)
      throws InterruptedException {
    LocalDateTime startTime = LocalDateTime.now();
    while (countFilesInDirectory(directoryPath) != expectedCount) {
      if (Duration.between(startTime, LocalDateTime.now()).toSeconds() > expectedCount * 1.2) {
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
