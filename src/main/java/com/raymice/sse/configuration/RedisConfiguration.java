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
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration class for Redis integration
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisConfiguration {

  @NotBlank private String host; // spring.data.redis.host
  @NotNull private Integer port; // spring.data.redis.port
  @NotNull private Integer timeout; // spring.data.redis.timeout
  @NotBlank private String repositoryName; // spring.data.redis.repositoryName

  /**
   * Defines the Redis Connection Factory using Lettuce
   *
   * @return the Redis connection factory
   */
  @Bean("myRedisConnectionFactory")
  public RedisConnectionFactory redisConnectionFactory() {
    LettuceClientConfiguration clientConfig =
        LettuceClientConfiguration.builder()
            .commandTimeout(java.time.Duration.ofMillis(timeout))
            .build();

    // Need to be updating according to the Redis server settings
    org.springframework.data.redis.connection.RedisConfiguration redisConfig =
        new org.springframework.data.redis.connection.RedisStandaloneConfiguration(host, port);

    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
    return factory;
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
    // Group the keys in Redis under repositoryName provided in application
    // properties
    return new SpringRedisIdempotentRepository(redisTemplate, repositoryName);
  }
}
