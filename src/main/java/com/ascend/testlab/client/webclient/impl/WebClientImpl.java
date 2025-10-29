package com.ascend.testlab.client.webclient.impl;

import com.ascend.testlab.client.datadog.DDClient;
import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.WebClientConfig;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.util.CommonUtil;
import com.google.inject.Inject;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.Vertx;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebClientImpl implements WebClient {

  private final io.vertx.rxjava3.ext.web.client.WebClient webClient;
  private CircuitBreaker circuitBreaker;
  private final DDClient ddClient;

  @Inject
  public WebClientImpl(Vertx vertx, WebClientConfig webClientConfig, DDClient ddClient) {
    this.webClient =
        io.vertx.rxjava3.ext.web.client.WebClient.create(
            vertx, getWebClientOptions(webClientConfig));
    this.ddClient = ddClient;
  }

  @Override
  public Completable close() {
    return Completable.fromAction(webClient::close);
  }

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

  private void pushCircuitBreakerMetricsToDD() {
    if (Objects.isNull(circuitBreaker)) return;

    String tag = CommonUtil.getCircuitBreakerTag(circuitBreaker.getName());
    CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

    pushGaugeMetricToDD(WebConstants.STATE, circuitBreaker.getState().getOrder(), tag);
    pushGaugeMetricToDD(WebConstants.BUFFERED_CALLS_COUNT, metrics.getNumberOfBufferedCalls(), tag);
    pushGaugeMetricToDD(
        WebConstants.NOT_PERMITTED_CALLS_COUNT, metrics.getNumberOfNotPermittedCalls(), tag);
    pushGaugeMetricToDD(
        WebConstants.SUCCESSFUL_CALLS_COUNT, metrics.getNumberOfSuccessfulCalls(), tag);
    pushGaugeMetricToDD(WebConstants.FAILED_CALLS_COUNT, metrics.getNumberOfFailedCalls(), tag);
    pushGaugeMetricToDD(WebConstants.SLOW_CALLS_COUNT, metrics.getNumberOfSlowCalls(), tag);
    pushGaugeMetricToDD(
        WebConstants.SLOW_SUCCESSFUL_CALLS_COUNT, metrics.getNumberOfSlowSuccessfulCalls(), tag);
    pushGaugeMetricToDD(
        WebConstants.SLOW_FAILED_CALLS_COUNT, metrics.getNumberOfSlowFailedCalls(), tag);
    pushGaugeMetricToDD(WebConstants.SLOW_CALL_RATE, metrics.getSlowCallRate(), tag);
    pushGaugeMetricToDD(WebConstants.FAILURE_RATE, metrics.getFailureRate(), tag);
  }

  private <T extends Number> void pushGaugeMetricToDD(
      String aspectName, T metricValue, String... tags) {
    this.ddClient.gauge(CommonUtil.getCircuitBreakerAspect(aspectName), metricValue, tags);
  }
}
