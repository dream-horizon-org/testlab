package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the circuit breaker.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class CircuitBreakerConfig {
  /** The failure rate threshold. */
  private int failureRateThreshold;

  /** The slow call rate threshold. */
  private int slowCallRateThreshold;

  /** The wait duration in open state. */
  private int waitDurationInOpenState;

  /** The slow call duration threshold. */
  private int slowCallDurationThreshold;

  /** The permitted number of calls in half open state. */
  private int permittedNumberOfCallsInHalfOpenState;

  /** The minimum number of calls. */
  private int minimumNumberOfCalls;

  /** The sliding window size. */
  private int slidingWindowSize;

  /**
   * Get the provider for the circuit breaker config.
   *
   * @return the provider for the circuit breaker config
   * @see ConfigProvider
   */
  public static ConfigProvider<CircuitBreakerConfig> provider() {
    return new ConfigProvider<>("circuit-breaker", CircuitBreakerConfig.class);
  }
}
