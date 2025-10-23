/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.workflow;

import com.raymice.swift.configuration.RoutingConfig;
import java.io.File;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest()
@ActiveProfiles("test")
public class End2EndTest {

  @Autowired private RoutingConfig routingConfig;

  @BeforeEach
  void beforeEach() throws Exception {
    // Clean up input and output directories before each test
    log.info("🧹Cleaning up input and output directories before test");
    FileUtils.cleanDirectory(new File(routingConfig.getInput().getPath().toString()));
    FileUtils.cleanDirectory(new File(routingConfig.getOutput().getInProgress().getPath()));
    FileUtils.cleanDirectory(new File(routingConfig.getOutput().getUnsupported().getPath()));
    FileUtils.cleanDirectory(new File(routingConfig.getOutput().getSuccess().getPath()));
    FileUtils.cleanDirectory(new File(routingConfig.getOutput().getError().getPath()));
  }

  @Test
  void receivePacs008_001_08() throws Exception {

    String inputWorkflowPath = routingConfig.getInput().getPath().toString();
    String outputSuccessPath = routingConfig.getOutput().getSuccess().getPath();

    String inputFileName = "pacs.008.001.08.xml";

    // Copy of the input file to the input directory will trigger the Camel route
    FileUtils.copyFile(
        new File("src/test/resources/%s".formatted(inputFileName)),
        new File("%s/%s".formatted(inputWorkflowPath, inputFileName)));

    Thread.sleep(2000); // Wait for 2 seconds to allow Camel route to process the file

    // Assert that the final step has been reached by checking the existence of the output file
    Optional<File> resultFileOpt =
        FileUtils.listFiles(
                new File(outputSuccessPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
            .stream()
            .findFirst();

    if (resultFileOpt.isEmpty()) {
      throw new AssertionError("Output file not found in success directory");
    } else {
      log.info("✅ Output file found: {}", resultFileOpt.get().getAbsolutePath());
    }

    assert (resultFileOpt.get().exists());
  }
}
