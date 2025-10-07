package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CircuitBreakerConfig {
  private int failureRateThreshold;
  private int slowCallRateThreshold;
  private int waitDurationInOpenState;
  private int slowCallDurationThreshold;
  private int permittedNumberOfCallsInHalfOpenState;
  private int minimumNumberOfCalls;
  private int slidingWindowSize;

  public static ConfigProvider<CircuitBreakerConfig> provider() {
    return new ConfigProvider<>("circuit-breaker", CircuitBreakerConfig.class);
  }
}
