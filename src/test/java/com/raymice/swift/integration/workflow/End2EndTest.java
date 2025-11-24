/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.workflow;

import static com.raymice.swift.TestingUtils.cleanDirectories;
import static com.raymice.swift.TestingUtils.copyFile;
import static com.raymice.swift.TestingUtils.hasFileInDirectory;
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
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
public class End2EndTest {

  @Autowired private ApplicationConfig applicationConfig;
  @Autowired private CamelContext camelContext;
  @Autowired private ProcessService processService;
  @Container private static final Containers containers = new Containers();

  @BeforeEach
  void beforeEach() throws Exception {
    // Clean up input and output directories before each test
    log.info("🧹Cleaning up input and output directories before test");
    cleanDirectories(
        applicationConfig.getFileInputPath(),
        applicationConfig.getFileOutputUnsupportedPath(),
        applicationConfig.getFileOutputSuccessPath(),
        applicationConfig.getFileOutputErrorPath());

    log.info("Cleaning DB before test");
    processService.deleteAll();
  }

  @Test
  void unsupportedExtension_movesFileToUnsupportedDirectory()
      throws IOException, InterruptedException {

    final String inputWorkflowPath = applicationConfig.getFileInputPath();
    final String outputUnsupportedPath = applicationConfig.getFileOutputUnsupportedPath();
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

    final String inputWorkflowPath = applicationConfig.getFileInputPath();
    final String outputErrorPath = applicationConfig.getFileOutputErrorPath();
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

    final String inputWorkflowPath = applicationConfig.getFileInputPath();
    final String outputSuccessPath = applicationConfig.getFileOutputSuccessPath();
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
    final String inputWorkflowPath = applicationConfig.getFileInputPath();
    final String outputSuccessPath = applicationConfig.getFileOutputSuccessPath();

    // Stop the route to prepare for batch processing
    camelContext.getRouteController().stopRoute(FileRoute.class.getSimpleName());

    final String testFileName = "pacs.008.001.08.xml";
    final int iterations = 100;
    final Duration expectedDuration = Duration.of(5, java.time.temporal.ChronoUnit.SECONDS);

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

    final String inputWorkflowPath = applicationConfig.getFileInputPath();
    final String outputUnsupportedPath = applicationConfig.getFileOutputUnsupportedPath();
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
  void shutdownActiveMq(CapturedOutput output) throws Exception {
    testContainerShutdown(output, Containers.ACTIVEMQ_IMAGE, 100, 2000);
  }

  @Test
  void shutdownRedis(CapturedOutput output) throws Exception {
    testContainerShutdown(output, Containers.REDIS_IMAGE, 1000, 2000);
  }

  @Test
  void shutdownPostgres(CapturedOutput output) throws Exception {
    testContainerShutdown(output, Containers.POSTGRES_IMAGE, 100, 2000);
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
  private void testContainerShutdown(
      CapturedOutput output, final String containerName, final int iterations, final int sleepTime)
      throws Exception {
    final String inputWorkflowPath = applicationConfig.getFileInputPath();
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
    assertTrue(hasFileInDirectory(applicationConfig.getFileOutputSuccessPath(), iterations));

    // Assert the process status is set to COMPLETED in database
    assertStatusInDatabase(ProcessEntity.Status.COMPLETED, iterations);

    // Check container killed
    String regex = "(\uD83D\uDCA5 Container )[a-f0-9]{64}( killed successfully)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(output.getOut());
    assertThat(matcher.find());

    // Check container started
    regex = "(\uD83C\uDFC1 Container )[a-f0-9]{64}( started successfully)";
    pattern = Pattern.compile(regex);
    matcher = pattern.matcher(output.getOut());
    assertThat(matcher.find());
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

      log.info("\uD83C\uDFC1 Container {} started successfully.", containerId);

    } catch (Exception e) {
      log.error("Error starting container {}: {}", containerId, e.getMessage());
      throw new RuntimeException();
    }
  }

  /**
   * Asserts that the process entities in the database match the expected status and count.
   *
   * <p>This method retrieves all process entities from the database and verifies that:
   * <ul>
   *   <li>The total number of entities matches the expected count</li>
   *   <li>Each entity has the expected status</li>
   * </ul>
   *
   * @param expectedStatus The expected status of all process entities
   * @param expectedCount The expected number of process entities in the database
   * @throws AssertionError If the actual count or status doesn't match the expected values
   */
  private void assertStatusInDatabase(ProcessEntity.Status expectedStatus, int expectedCount) {
    List<ProcessEntity> processEntities = processService.findAll();
    assertEquals(expectedCount, processEntities.size());
    for (ProcessEntity entity : processEntities) {
      assertEquals(expectedStatus, entity.getStatus());
    }
  }
}
