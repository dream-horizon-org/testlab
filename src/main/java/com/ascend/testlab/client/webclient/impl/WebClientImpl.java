package com.ascend.testlab.client.webclient.impl;

import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.config.WebClientConfig;
import com.ascend.testlab.exception.ErrorEnum;
import com.google.inject.Inject;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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

  /** {@inheritDoc} */
  @Override
  public <R> Maybe<R> sendHTTPGETRequest(
      ApplicationConfig.ServiceConfig serviceConfig,
      Map<String, String> queryParams,
      MultiMap headers,
      ErrorEnum errorEnum,
      Function<JsonObject, R> responseMapper) {

    Integer port = serviceConfig.getPort();
    String host = serviceConfig.getServiceURL();
    String endPoint = serviceConfig.getApiEndPoint();

    HttpRequest<Buffer> request =
        webClient
            .get(port, host, endPoint)
            .putHeaders(headers)
            .timeout(serviceConfig.getApiTimeoutMS());
    queryParams.forEach(request::addQueryParam);

    return request
        .rxSend()
        .flatMapMaybe(response -> Maybe.just(responseMapper.apply(response.bodyAsJsonObject())))
        .onErrorResumeNext(
            err -> {
              log.error("Error in GET request {}{}: ", host, endPoint, err);
              return Maybe.empty();
            });
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
