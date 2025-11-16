/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.workflow;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.KillContainerCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.raymice.swift.configuration.ApplicationConfig;
import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.exception.MalformedXmlException;
import com.raymice.swift.integration.Containers;
import com.raymice.swift.routing.read.FileRoute;
import jakarta.annotation.PostConstruct;
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

  @Autowired private ApplicationConfig applicationConfig;
  @Autowired private CamelContext camelContext;
  @Autowired private ProcessService processService;
  @Container private static final Containers containers = new Containers();

  private String inputFilePath;
  private String successFilePath;
  private String unsupportedFilePath;
  private String errorFilePath;

  @PostConstruct
  void postConstruct() {
    var routing = applicationConfig.getRouting();
    var fileConfig = routing.getFile();
    var fileOutput = fileConfig.getOutput();
    inputFilePath = fileConfig.getInput().getPath().toString();
    successFilePath = fileOutput.getSuccess().getPath();
    unsupportedFilePath = fileOutput.getUnsupported().getPath();
    errorFilePath = fileOutput.getError().getPath();
  }

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    containers.applyDynamicProperties(registry);
  }

  @BeforeEach
  void beforeEach() throws Exception {
    // Clean up input and output directories before each test
    log.info("🧹Cleaning up input and output directories before test");
    cleanDirectories(inputFilePath, unsupportedFilePath, successFilePath, errorFilePath);

    log.info("Cleaning DB before test");
    processService.deleteAll();
  }

  @Test
  void unsupportedExtension_movesFileToUnsupportedDirectory()
      throws IOException, InterruptedException {

    final String inputWorkflowPath = inputFilePath;
    final String outputUnsupportedPath = unsupportedFilePath;
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

    final String inputWorkflowPath = inputFilePath;
    final String outputErrorPath = errorFilePath;
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

    final String inputWorkflowPath = inputFilePath;
    final String outputSuccessPath = successFilePath;
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
    final String inputWorkflowPath = inputFilePath;
    final String outputSuccessPath = successFilePath;

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

  @Test
  void processUnsupportedMxFileType_movesFileToUnsupportedDirectory()
      throws IOException, InterruptedException {

    final String inputWorkflowPath = inputFilePath;
    final String outputUnsupportedPath = unsupportedFilePath;
    final String inputFileName = "pacs.unsupported-type.xml";
    final String testFilePath = "src/test/resources/mx/%s".formatted(inputFileName);
    final String inputFilePath = "%s/%s".formatted(inputWorkflowPath, inputFileName);

    // Copy of the input file to the input directory will trigger the Camel route
    copyFile(testFilePath, inputFilePath);

    // Assert that the file is moved to the unsupported directory (end of processing)
    assertTrue(hasFileInDirectory(outputUnsupportedPath, 1));

    // Assert the process status is set to UNSUPPORTED in database
    assertStatusInDatabase(ProcessEntity.Status.UNSUPPORTED, 1);
  }

  @Test
  void shutdownActiveMq() throws Exception {
    testContainerShutdown(Containers.ACTIVEMQ_IMAGE, 100, 4000);
  }

  @Test
  void shutdownRedis() throws Exception {
    testContainerShutdown(Containers.REDIS_IMAGE, 1000, 5000);

    // TODO Check "Reconnected to localhost/<unresolved>:3333"
  }

  /**
   * Tests the container shutdown scenario by stopping the Camel route, copying test files to the input directory,
   * starting the Camel route to process the batch of files, simulating container shutdown, waiting for downtime,
   * restarting the container, and verifying that all files are processed and the process status is set to COMPLETED.
   *
   * @param containerName The name of the container to test.
   * @param iterations The number of iterations to perform the test.
   * @param sleepTime The time to wait between operations in milliseconds.
   * @throws Exception If an error occurs during the test.
   */
  private void testContainerShutdown(String containerName, int iterations, int sleepTime)
      throws Exception {
    final String inputWorkflowPath = inputFilePath;
    final String testFileName = "pacs.008.001.08.xml";
    final String containerId = containers.getContainers().get(containerName).getContainerId();

    // Stop the route to prepare for batch processing
    camelContext.getRouteController().stopRoute(FileRoute.class.getSimpleName());

    for (int i = 0; i < iterations; i++) {
      String testFilePath = "src/test/resources/mx/%s".formatted(testFileName);
      String inputFilePath = "%s/%d-%s".formatted(inputWorkflowPath, i, testFileName);
      copyFile(testFilePath, inputFilePath);
    }

    // Start the route to process the batch of files
    camelContext.getRouteController().startRoute(FileRoute.class.getSimpleName());

    // Wait a moment to let some files be processed
    Thread.sleep(sleepTime);

    // Shutdown the container (not using Testcontainers stop to simulate unexpected shutdown)
    killContainer(containerId);

    // Wait a moment to simulate downtime
    Thread.sleep(sleepTime * 3L);

    // Restart the container (keep same ports)
    startContainer(containerId);

    // Wait to find all files in the success directory (end of processing)
    assertTrue(hasFileInDirectory(successFilePath, iterations));

    // Assert the process status is set to COMPLETED in database
    assertStatusInDatabase(ProcessEntity.Status.COMPLETED, iterations);
  }

  /**
   * Opens a Docker client with default configuration.
   *
   * @return A DockerClient instance configured with default settings.
   */
  private static DockerClient openDockerClient() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    DockerHttpClient httpClient =
        new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

    return DockerClientImpl.getInstance(config, httpClient);
  }

  /**
   * Kills a Docker container with the specified ID.
   *
   * <p>This method attempts to kill a Docker container using the provided container ID.
   * It uses the Docker client to execute the kill command. If the operation fails,
   * it logs an error message and throws a RuntimeException.</p>
   *
   * @param containerId The ID of the Docker container to be killed.
   * @throws RuntimeException If an error occurs while killing the container.
   */
  private void killContainer(String containerId) throws RuntimeException {
    try (DockerClient dockerClient = openDockerClient()) {

      // Create a KillContainerCmd
      KillContainerCmd killCmd = dockerClient.killContainerCmd(containerId);

      // Execute the command to kill the container
      killCmd.exec();

      log.info("\uD83D\uDCA5 Container {} killed successfully.", containerId);

    } catch (Exception e) {
      log.error("Error killing container {}: {}", containerId, e.getMessage());
      throw new RuntimeException();
    }
  }

  /**
   * Starts a Docker container with the specified container ID.
   *
   * @param containerId The ID of the Docker container to start.
   * @throws RuntimeException If an error occurs while starting the container.
   */
  private void startContainer(String containerId) throws RuntimeException {
    try (DockerClient dockerClient = openDockerClient()) {

      // Start the container
      dockerClient.startContainerCmd(containerId).exec();

      log.info("Container {} started successfully.", containerId);

    } catch (Exception e) {
      log.error("Error starting container {}: {}", containerId, e.getMessage());
      throw new RuntimeException();
    }
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
