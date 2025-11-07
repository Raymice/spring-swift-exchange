/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.workflow;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.exception.MalformedXmlException;
import com.raymice.swift.routing.read.FileRoute;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
@SpringBootTest()
@ActiveProfiles("test")
public class End2EndTest {

  @Autowired private RoutingConfig routingConfig;
  @Autowired private CamelContext camelContext;

  @Container
  static GenericContainer<?> activemq =
      new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:6.1.7"))
          .withExposedPorts(61616);

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:8.2.2")).withExposedPorts(6379);

  @DynamicPropertySource
  static void updateProperties(DynamicPropertyRegistry registry) throws InterruptedException {
    final String activemqHost = activemq.getHost();
    final Integer activemqPort = activemq.getMappedPort(61616);
    final String redisHost = redis.getHost();
    final Integer redisPort = redis.getMappedPort(6379);

    registry.add(
        "spring.activemq.broker-url", () -> "tcp://%s:%d".formatted(activemqHost, activemqPort));
    log.info("ℹ️ActiveMQ broker URL updated: tcp://{}:{}", activemqHost, activemqPort);

    registry.add("spring.redis.host", () -> redisHost);
    registry.add("spring.redis.port", () -> redisPort);
    log.info("ℹ️Redis connection updated: {}:{}", redisHost, redisPort);
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
    assertTrue(hasFileInDirectory(outputUnsupportedPath));
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
    assertTrue(hasFileInDirectory(outputErrorPath));

    // Verify that MalformedXmlException was throw during processing
    assertThat(output.getOut()).contains(new MalformedXmlException().getMessage());
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
    assertTrue(hasFileInDirectory(outputSuccessPath));
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
    while (countFilesInDirectory(outputSuccessPath) != iterations) {
      Thread.sleep(10);
    }

    final Duration totalDuration = Duration.between(now, LocalDateTime.now());

    log.info("⏱ Total time to process {} files: {} seconds", iterations, totalDuration.toSeconds());

    assertTrue(
        totalDuration.compareTo(expectedDuration) < 0,
        "Processing time exceeded expected threshold");
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

  private boolean hasFileInDirectory(String directoryPath) throws InterruptedException {
    Thread.sleep(2000); // Wait for 2 seconds to allow Camel route to process the file

    Optional<File> resultFileOpt = getFileInDirectory(directoryPath);
    if (resultFileOpt.isEmpty()) {
      throw new AssertionError("Output file not found in expected directory");
    } else {
      log.info("✅Output file found: {}", resultFileOpt.get().getAbsolutePath());
      return true;
    }
  }
}
