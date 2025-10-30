/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

// Will be automatically picked up by Spring Boot Actuator
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

  @Autowired
  @Qualifier("myRedisConnectionFactory")
  private RedisConnectionFactory redisConnectionFactory;

  @Override
  public Health health() {
    try {
      // Attempt a simple operation like pinging Redis
      redisConnectionFactory.getConnection().ping();
      return Health.up().build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
