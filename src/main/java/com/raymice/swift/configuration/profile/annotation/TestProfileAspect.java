/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.configuration.profile.annotation;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TestProfileAspect {

  private final Environment environment;

  public TestProfileAspect(Environment environment) {
    this.environment = environment;
  }

  @Around("@annotation(TestProfileOnly)")
  public Object checkProfile(ProceedingJoinPoint joinPoint) throws Throwable {
    if (isTestProfileActive()) {
      return joinPoint.proceed();
    } else {
      throw new IllegalStateException("Method is only allowed when 'test' profile is active.");
    }
  }

  private boolean isTestProfileActive() {
    return Arrays.asList(environment.getActiveProfiles()).contains("test");
  }
}
