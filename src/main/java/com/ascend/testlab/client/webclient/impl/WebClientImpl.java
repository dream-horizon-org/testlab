package com.ascend.testlab.client.webclient.impl;

import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.config.WebClientConfig;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import com.google.inject.Inject;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.rxjava3.circuitbreaker.operator.CircuitBreakerOperator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
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
  public <R> Single<R> sendHTTPGETRequest(
      ApplicationConfig.ServiceConfig serviceConfig,
      Map<String, String> queryParams,
      MultiMap headers,
      ErrorEnum errorEnum,
      Function<HttpResponse<Buffer>, R> responseMapper) {

    String host = serviceConfig.getServiceURL();
    String endPoint = serviceConfig.getApiEndPoint();
    int port = serviceConfig.getPort();

    HttpRequest<Buffer> request =
        webClient
            .get(port, host, endPoint)
            .putHeaders(headers)
            .timeout(serviceConfig.getApiTimeoutMS());
    queryParams.forEach(request::addQueryParam);

    return request
        .rxSend()
        .retry(serviceConfig.getRetryCount(), throwable -> !(throwable instanceof TimeoutException))
        .flatMap(response -> composeResponseWithCircuitBreaker(response, errorEnum, responseMapper))
        .doOnError(err -> log.error("Error in GET request {}{}: ", host, endPoint, err));
  }

  private <U> Single<U> composeResponseWithCircuitBreaker(
      HttpResponse<Buffer> response,
      ErrorEnum errorEnum,
      Function<HttpResponse<Buffer>, U> responseMapper) {

    return Optional.ofNullable(this.circuitBreaker)
        .map(
            cb ->
                Single.just(handleResponse(response, errorEnum, responseMapper))
                    .compose(CircuitBreakerOperator.of(cb)))
        .orElseGet(() -> Single.just(handleResponse(response, errorEnum, responseMapper)));
  }

  private <U> U handleResponse(
      HttpResponse<Buffer> response,
      ErrorEnum errorEnum,
      Function<HttpResponse<Buffer>, U> responseMapper) {
    if (response.statusCode() > 399)
      return handleFailureResponse(response, errorEnum, responseMapper);
    else return handleSuccessResponse(response, responseMapper);
  }

  private <U> U handleSuccessResponse(
      HttpResponse<Buffer> response, Function<HttpResponse<Buffer>, U> responseMapper) {
    return responseMapper.apply(response);
  }

  private <U> U handleFailureResponse(
      HttpResponse<Buffer> response,
      ErrorEnum errorEnum,
      Function<HttpResponse<Buffer>, U> responseMapper) {
    log.error("Error response [{}]: {}", response.statusCode(), response.bodyAsJsonObject());
    if (response.statusCode() > 499) {
      /* throw error on 5xx errors to open Circuit Breaker */
      throwErrorOn5xxResponse(errorEnum, response.bodyAsString());
    }
    return responseMapper.apply(response);
  }

  private void throwErrorOn5xxResponse(ErrorEnum errorEnum, String responseBody) {
    printCircuitBreakerState();
    throw ExceptionUtil.getException(errorEnum, responseBody);
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
