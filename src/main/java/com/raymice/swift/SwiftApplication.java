/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift;

import org.apache.camel.opentelemetry.starter.CamelOpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@CamelOpenTelemetry
@SpringBootApplication
public class SwiftApplication {

  static void main(String[] args) {
    SpringApplication.run(SwiftApplication.class, args);
  }
}
