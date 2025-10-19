/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class ActuatorDevTests {

  private final String path = "/actuator";

  @Autowired private MockMvc mockMvc;

  @Test
  void getHealth() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void getLiveness() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health/liveness", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void getReadiness() throws Exception {
    mockMvc
        .perform(get(String.format("%s/health/readiness", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void getInfo() throws Exception {
    mockMvc
        .perform(get(String.format("%s/info", path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.app").exists())
        .andExpect(jsonPath("$.app.name").value("swift"))
        .andExpect(jsonPath("$.java").exists());
  }

  @Test
  void getMetrics() throws Exception {
    mockMvc.perform(get(String.format("%s/metrics", path))).andExpect(status().isOk());
  }

  @Test
  void getJvmMemoryUsed() throws Exception {
    mockMvc
        .perform(get(String.format("%s/metrics/jvm.memory.used", path)))
        .andExpect(status().isOk());
  }
}
