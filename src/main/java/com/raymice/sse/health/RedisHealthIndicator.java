/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Redis
 * Will be automatically picked up by Spring Boot Actuator
 */
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

  @Autowired
  @Qualifier("myRedisConnectionFactory")
  private RedisConnectionFactory redisConnectionFactory;

  @Override
  public Health health() {
    try {
      // Ping the Redis server to check its health
      redisConnectionFactory.getConnection().ping();
      return Health.up().build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
