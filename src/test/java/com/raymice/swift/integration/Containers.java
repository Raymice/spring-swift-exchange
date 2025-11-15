/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.integration;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

  public static final String ACTIVEMQ_IMAGE = "apache/activemq-classic:6.1.7";
  public static final String POSTGRES_IMAGE = "postgres:17-alpine";
  public static final String REDIS_IMAGE = "redis:8.2.2";

  private static final HashMap<String, GenericContainer<?>> containers = new HashMap<>();
  private static GenericContainer<?> postgres;
  private static GenericContainer<?> activemq;
  private static GenericContainer<?> redis;

  public Containers() {
    this.createContainers();
  }

  public HashMap<String, GenericContainer<?>> getContainers() {
    return containers;
  }

  @Override
  public void start() {
    // Parallelize startup of containers
    try (ExecutorService executor = Executors.newFixedThreadPool(containers.size())) {
      List<Future<?>> futures = new ArrayList<>();

      for (GenericContainer<?> container : containers.values()) {
        futures.add(
            executor.submit(
                () -> {
                  log.info("🚀 Starting container: {}", container.getDockerImageName());
                  container.start();
                  try {
                    // Wait to avoid issues when running all the tests at once
                    Thread.sleep(2000);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                  log.info("\uD83C\uDFC3 Container started: {}", container.getDockerImageName());
                }));
      }

      // Wait for all tasks to complete
      for (Future<?> future : futures) {
        try {
          // This will block until the task is completed
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException("Failed to start containers in parallel", e);
        }
      }
    }
  }

  @Override
  public void stop() {
    for (GenericContainer<?> container : containers.values()) {
      log.info("🛑Stopping container: {}", container.getDockerImageName());
      container.stop();
    }
  }

  private void createContainers() {

    activemq =
        new GenericContainer<>(DockerImageName.parse(ACTIVEMQ_IMAGE))
            .withCreateContainerCmdModifier(
                cmd -> {
                  Objects.requireNonNull(cmd.getHostConfig())
                      .withPortBindings(
                          new PortBinding(
                              // Force map container port 1111 to host port 61616 for automatic
                              // reconnection during testing
                              Ports.Binding.bindPort(1111), new ExposedPort(61616)));
                });

    postgres =
        new GenericContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
            .withCreateContainerCmdModifier(
                cmd -> {
                  Objects.requireNonNull(cmd.getHostConfig())
                      .withPortBindings(
                          new PortBinding(
                              // Force map container port 2222 to host port 5432 for automatic
                              // reconnection during testing
                              Ports.Binding.bindPort(2222), new ExposedPort(5432)));
                })
            .withEnv("POSTGRES_DB", pgDatabase) // Set database name
            .withEnv("POSTGRES_USER", pgUser)
            .withEnv("POSTGRES_PASSWORD", pgPassword);

    redis =
        new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
            .withCreateContainerCmdModifier(
                cmd -> {
                  Objects.requireNonNull(cmd.getHostConfig())
                      .withPortBindings(
                          new PortBinding(
                              // Force map container port 3333 to host port 6379 for automatic
                              // reconnection during testing
                              Ports.Binding.bindPort(3333), new ExposedPort(6379)));
                });

    containers.put(ACTIVEMQ_IMAGE, activemq);
    containers.put(POSTGRES_IMAGE, postgres);
    containers.put(REDIS_IMAGE, redis);
  }

  public void applyDynamicProperties(DynamicPropertyRegistry registry) {
    final String host = activemq.getHost();
    final Integer port = activemq.getMappedPort(61616);

    registry.add(
        "spring.activemq.broker-url",
        () -> "failover:(tcp://%s:%d)?startupMaxReconnectAttempts=-1".formatted(host, port));
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
