package com.hulk.testlab.client.webclient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.codegen.annotations.Fluent;

public interface WebClient {

  Completable close();
  
    @Fluent
  WebClient setCircuitBreaker(CircuitBreaker circuitBreaker);
  }
