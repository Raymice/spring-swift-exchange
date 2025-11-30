/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration class for Redis integration
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfiguration {

  @NotBlank private String host;
  @NotNull private int port;

  /**
   * Defines the Redis Connection Factory using Lettuce
   *
   * @return the Redis connection factory
   */
  @Bean("myRedisConnectionFactory")
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  /**
   * Defines a RedisTemplate for String keys and values
   *
   * @param redisConnectionFactory the Redis connection factory
   * @return the Redis template
   */
  @Bean
  public RedisTemplate<String, String> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    return redisTemplate;
  }

  /**
   * Defines a Redis-based Idempotent Repository for file processing (will be used
   * as a shared read lock)
   *
   * @param redisTemplate the Redis template
   * @return the Idempotent Repository
   */
  @Bean("myRedisIdempotentRepository")
  public SpringRedisIdempotentRepository myRedisIdempotentRepository(
      RedisTemplate<String, String> redisTemplate) {
    // Group the keys in Redis under "myFileProcessor"
    // TODO externalize the repository name
    return new SpringRedisIdempotentRepository(redisTemplate, "myFileProcessor");
  }
}
