package com.ascend.testlab.util;

import com.ascend.testlab.config.CircuitBreakerConfig;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class CircuitBreakerFactory {
  private static final CircuitBreakerRegistry circuitBreakerRegistry =
      CircuitBreakerRegistry.ofDefaults();
  @Inject private static CircuitBreakerConfig circuitBreakerConfig;

  public static CircuitBreaker getCircuitBreaker(String name) {
    return circuitBreakerRegistry.circuitBreaker(name, getCircuitBreakerConfig());
  }

  private static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
      getCircuitBreakerConfig() {
    return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
        .failureRateThreshold(circuitBreakerConfig.getFailureRateThreshold())
        .slowCallRateThreshold(circuitBreakerConfig.getSlowCallRateThreshold())
        .waitDurationInOpenState(
            Duration.ofMillis(circuitBreakerConfig.getWaitDurationInOpenState()))
        .slowCallDurationThreshold(
            Duration.ofMillis(circuitBreakerConfig.getSlowCallDurationThreshold()))
        .permittedNumberOfCallsInHalfOpenState(
            circuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState())
        .minimumNumberOfCalls(circuitBreakerConfig.getMinimumNumberOfCalls())
        .slidingWindowType(
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType
                .COUNT_BASED)
        .slidingWindowSize(circuitBreakerConfig.getSlidingWindowSize())
        .recordExceptions(
            IOException.class, TimeoutException.class, Exception.class, RestException.class)
        .build();
  }
}
