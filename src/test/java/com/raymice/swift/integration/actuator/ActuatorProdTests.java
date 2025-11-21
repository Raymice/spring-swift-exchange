/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.raymice.swift.integration.Containers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles({"prod", "test"})
@AutoConfigureMockMvc
class ActuatorProdTests {

  private final String path = "/actuator";

  @Autowired private MockMvc mockMvc;

  @Container private static final Containers containers = new Containers();

  @Order(1)
  @Test
  void getInfo() throws Exception {
    mockMvc.perform(get(String.format("%s/info", path))).andExpect(status().isNotFound());
  }

  @Order(2)
  @Test
  void getMetrics() throws Exception {
    mockMvc.perform(get(String.format("%s/metrics", path))).andExpect(status().isNotFound());
  }

  @Order(3)
  @Test
  void getHealth() throws Exception {
    Thread.sleep(1000); // Wait for 1 seconds to allow all health indicators to initialize
    mockMvc
        .perform(get(String.format("%s/health", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Order(4)
  @Test
  void getLiveness() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health/liveness", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Order(5)
  @Test
  void getReadiness() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health/readiness", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
