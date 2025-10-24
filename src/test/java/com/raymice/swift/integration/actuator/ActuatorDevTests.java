/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
@AutoConfigureMockMvc
class ActuatorDevTests {

  private final String path = "/actuator";

  @Autowired private MockMvc mockMvc;

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

  @Order(1)
  @Test
  void getInfo() throws Exception {
    mockMvc
        .perform(get(String.format("%s/info", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.app").exists())
        .andExpect(jsonPath("$.app.name").value("swift"))
        .andExpect(jsonPath("$.java").exists());
  }

  @Order(2)
  @Test
  void getMetrics() throws Exception {
    mockMvc.perform(get(String.format("%s/metrics", path))).andExpect(status().isOk());
  }

  @Order(3)
  @Test
  void getJvmMemoryUsed() throws Exception {
    mockMvc
        .perform(get(String.format("%s/metrics/jvm.memory.used", path)))
        .andExpect(status().isOk());
  }

  @Order(4)
  @Test
  void getHealth() throws Exception {
    Thread.sleep(1000); // Wait for 1 seconds to allow all health indicators to initialize
    mockMvc
        .perform(get(String.format("%s/health", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Order(5)
  @Test
  void getLiveness() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health/liveness", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Order(6)
  @Test
  void getReadiness() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health/readiness", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
