/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.workflow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raymice.swift.configuration.RoutingConfig;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

  @Container
  static GenericContainer<?> activemq =
      new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:6.1.7"))
          .withExposedPorts(61616);

  @DynamicPropertySource
  static void activemqProperties(DynamicPropertyRegistry registry) throws InterruptedException {
    final String host = activemq.getHost();
    final Integer port = activemq.getMappedPort(61616);

    registry.add("spring.activemq.broker-url", () -> "tcp://%s:%d".formatted(host, port));
    log.info("ℹ️ActiveMQ broker URL updated: tcp://{}:{}", host, port);
  }

  @BeforeEach
  void beforeEach() throws Exception {
    // Clean up input and output directories before each test
    log.info("🧹Cleaning up input and output directories before test");
    cleanDirectories(
        routingConfig.getInput().getPath().toString(),
        routingConfig.getOutput().getInProgress().getPath(),
        routingConfig.getOutput().getUnsupported().getPath(),
        routingConfig.getOutput().getSuccess().getPath(),
        routingConfig.getOutput().getError().getPath());
  }

  @Test
  void unsupportedExtension_movesFileToUnsupportedDirectory()
      throws IOException, InterruptedException {

    String inputWorkflowPath = routingConfig.getInput().getPath().toString();
    String outputUnsupportedPath = routingConfig.getOutput().getUnsupported().getPath();
    String inputFileName = "unsupported.json";
    final String testFilePath = "src/test/resources/%s".formatted(inputFileName);
    final String inputFilePath = "%s/%s".formatted(inputWorkflowPath, inputFileName);

    // Copy of the input file to the input directory will trigger the Camel route
    copyFile(testFilePath, inputFilePath);

    // Assert that the file is moved to the unsupported directory (end of processing)
    assertTrue(hasFileInDirectory(outputUnsupportedPath));
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

  private void copyFile(String sourcePath, String destinationPath) throws IOException {
    FileUtils.copyFile(new File(sourcePath), new File(destinationPath));
  }

  private void cleanDirectories(String... paths) throws IOException {
    for (String path : paths) {
      FileUtils.cleanDirectory(new File(path));
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
