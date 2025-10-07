package com.ascend.testlab.injection.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.AbstractModule;
import io.vertx.rxjava3.core.Vertx;

public class DefaultModule extends AbstractModule {

  private final Vertx vertx;

  private final ObjectMapper objectMapper =
      JsonMapper.builder()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
          .serializationInclusion(JsonInclude.Include.NON_NULL)
          .build();

  public DefaultModule(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  protected void configure() {
    bind(Vertx.class).toInstance(this.vertx);
    bind(ObjectMapper.class).toInstance(this.objectMapper);
  }
}
