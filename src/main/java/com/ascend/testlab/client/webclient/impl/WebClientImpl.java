package com.ascend.testlab.client.webclient.impl;

import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.WebClientConfig;
import com.google.inject.Inject;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.Vertx;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the WebClient interface.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see WebClient
 */
@Slf4j
public class WebClientImpl implements WebClient {

  /** The web client. */
  private final io.vertx.rxjava3.ext.web.client.WebClient webClient;

  /** The circuit breaker. */
  private CircuitBreaker circuitBreaker;

  /**
   * Constructor for the WebClientImpl.
   *
   * @param vertx the Vertx instance
   * @param webClientConfig the web client configuration
   */
  @Inject
  public WebClientImpl(Vertx vertx, WebClientConfig webClientConfig) {
    this.webClient =
        io.vertx.rxjava3.ext.web.client.WebClient.create(
            vertx, getWebClientOptions(webClientConfig));
  }

  /** {@inheritDoc} */
  @Override
  public Completable close() {
    return Completable.fromAction(webClient::close);
  }

  /** {@inheritDoc} */
  @Override
  public WebClient setCircuitBreaker(CircuitBreaker circuitBreaker) {
    this.circuitBreaker = circuitBreaker;
    return this;
  }

  private static WebClientOptions getWebClientOptions(WebClientConfig webClientConfig) {
    return new WebClientOptions()
        .setPipeliningLimit(webClientConfig.getPipeliningLimit())
        .setConnectTimeout(webClientConfig.getConnectTimeout())
        .setMaxPoolSize(webClientConfig.getMaxPoolSize())
        .setLogActivity(webClientConfig.isLogActivity())
        .setKeepAlive(webClientConfig.isKeepAlive())
        .setKeepAliveTimeout(webClientConfig.getKeepAliveTimeout())
        .setPipelining(webClientConfig.isPipelining());
  }

  private void printCircuitBreakerState() {

    if (Objects.isNull(circuitBreaker)) return;
    log.info(
        "CircuitBreaker:{} | Successful call count:{} | Failed call count:{} | Failure rate:{}% | State:{}",
        circuitBreaker.getName(),
        circuitBreaker.getMetrics().getNumberOfSuccessfulCalls(),
        circuitBreaker.getMetrics().getNumberOfFailedCalls(),
        circuitBreaker.getMetrics().getFailureRate(),
        circuitBreaker.getState());
  }
}
