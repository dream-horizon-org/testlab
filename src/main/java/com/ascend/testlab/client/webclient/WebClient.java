package com.ascend.testlab.client.webclient;

import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.exception.ErrorEnum;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
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

  <R> Maybe<R> sendHTTPGETRequest(
      ApplicationConfig.ServiceConfig serviceConfig,
      Map<String, String> queryParams,
      MultiMap headers,
      ErrorEnum errorEnum,
      Function<JsonObject, R> responseMapper);
}
