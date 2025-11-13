/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class Containers implements Startable {

  private static final String pgDatabase = "swiftdb";
  private static final String pgUser = "xxx";
  private static final String pgPassword = "xxx";

  private static final List<GenericContainer<?>> containers = new ArrayList<>();
  private static GenericContainer<?> postgres;
  private static GenericContainer<?> activemq;
  private static GenericContainer<?> redis;

  public Containers() {
    this.createContainers();
  }

  @Override
  public void start() {
    for (GenericContainer<?> container : containers) {
      log.info("🚀Starting container: {}", container.getDockerImageName());
      container.start();
    }
  }

  @Override
  public void stop() {
    for (GenericContainer<?> container : containers) {
      log.info("🛑Stopping container: {}", container.getDockerImageName());
      container.stop();
    }
  }

  private void createContainers() {

    activemq =
        new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:6.1.7"))
            .withExposedPorts(61616);

    postgres =
        new GenericContainer<>(DockerImageName.parse("postgres:17-alpine"))
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", pgDatabase) // Set database name
            .withEnv("POSTGRES_USER", pgUser)
            .withEnv("POSTGRES_PASSWORD", pgPassword);

    redis = new GenericContainer<>(DockerImageName.parse("redis:8.2.2")).withExposedPorts(6379);

    containers.add(activemq);
    containers.add(postgres);
    containers.add(redis);
  }

  public void applyDynamicProperties(DynamicPropertyRegistry registry) {
    final String host = activemq.getHost();
    final Integer port = activemq.getMappedPort(61616);

    registry.add("spring.activemq.broker-url", () -> "tcp://%s:%d".formatted(host, port));
    log.info("ℹ️ActiveMQ broker URL updated: tcp://{}:{}", host, port);

    final String postgresHost = postgres.getHost();
    final Integer postgresPort = postgres.getMappedPort(5432);

    registry.add(
        "spring.datasource.url",
        () -> "jdbc:postgresql://%s:%d/%s".formatted(postgresHost, postgresPort, pgDatabase));
    registry.add("spring.datasource.username", () -> pgUser);
    registry.add("spring.datasource.password", () -> pgPassword);
    log.info(
        "ℹ️Postgres connection updated: jdbc:postgresql://{}:{}/{}}",
        postgresHost,
        postgresPort,
        pgDatabase);

    final String redisHost = redis.getHost();
    final Integer redisPort = redis.getMappedPort(6379);

    registry.add("spring.redis.url", () -> redisHost);
    registry.add("spring.redis.port", () -> redisPort);
    log.info("ℹ️Redis connection updated: {}:{}", redisHost, redisPort);
  }
}
