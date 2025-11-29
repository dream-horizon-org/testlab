package com.ascend.testlab.client.webclient;

import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.exception.ErrorEnum;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import java.util.Map;
import java.util.function.Function;

/**
 * Interface for the client to interact with a web server. Contains methods to interact with the web
 * server.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface WebClient {

  /**
   * Close the client and release resources.
   *
   * @return a Completable that completes when the client is closed
   */
  Completable close();

  /**
   * Set the circuit breaker for the client.
   *
   * @param circuitBreaker the circuit breaker to set
   * @return the client
   */
  @Fluent
  WebClient setCircuitBreaker(CircuitBreaker circuitBreaker);

  /**
   * Sends an HTTP GET request.
   *
   * @param serviceConfig the service configuration
   * @param queryParams the query parameters
   * @param headers the headers
   * @param errorEnum the error enum to use in case of failure
   * @param responseMapper the function to map the response
   * @param <R> the type of the response
   * @return a Single emitting the mapped response
   */
  <R> Single<R> sendHTTPGETRequest(
      ApplicationConfig.ServiceConfig serviceConfig,
      Map<String, String> queryParams,
      MultiMap headers,
      ErrorEnum errorEnum,
      Function<HttpResponse<Buffer>, R> responseMapper);
}
