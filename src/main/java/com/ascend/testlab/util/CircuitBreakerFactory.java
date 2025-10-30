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

/**
 * Factory class for creating circuit breakers.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see CircuitBreaker
 * @see CircuitBreakerRegistry
 */
@UtilityClass
public final class CircuitBreakerFactory {
  /** The circuit breaker registry. */
  private static final CircuitBreakerRegistry circuitBreakerRegistry =
      CircuitBreakerRegistry.ofDefaults();

  /** The circuit breaker configuration. */
  @Inject static CircuitBreakerConfig circuitBreakerConfig;

  /**
   * Get a circuit breaker by name using the circuit breaker registry. If not found, create a new
   * one.
   *
   * @param name the name of the circuit breaker
   * @return the circuit breaker
   */
  public static CircuitBreaker getCircuitBreaker(String name) {
    return circuitBreakerRegistry.circuitBreaker(name, getCircuitBreakerConfig());
  }

  /**
   * Get the circuit breaker configuration.
   *
   * @return the circuit breaker configuration
   */
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
