package com.ascend.testlab.client.webclient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.codegen.annotations.Fluent;

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
}
