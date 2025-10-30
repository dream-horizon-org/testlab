package com.ascend.testlab.injection.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.AbstractModule;
import io.vertx.rxjava3.core.Vertx;
import java.util.Objects;

/**
 * Default module for the testlab application. Contains methods to bind the Vertx and ObjectMapper.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see AbstractModule
 */
public class DefaultModule extends AbstractModule {

  /** The Vertx instance. */
  private final Vertx vertx;

  /** The ObjectMapper instance. */
  private final ObjectMapper objectMapper =
      JsonMapper.builder()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
          .serializationInclusion(JsonInclude.Include.NON_NULL)
          .build();

  /**
   * Constructor for the DefaultModule.
   *
   * @param vertx the Vertx instance
   */
  public DefaultModule(Vertx vertx) {
    Objects.requireNonNull(vertx, "vertx cannot be null");
    this.vertx = vertx;
  }

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(Vertx.class).toInstance(this.vertx);
    bind(ObjectMapper.class).toInstance(this.objectMapper);
  }
}
