package com.ascend.testlab.provider;

import com.ascend.testlab.injection.GuiceInjector;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Custom ObjectMapper provider for Jackson configuration.
 *
 * <p>Uses the ObjectMapper instance configured and bound in DefaultModule to ensure consistent
 * Jackson configuration across the application.
 *
 * <p>Note: This provider uses a no-arg constructor (required by JAX-RS) and obtains the
 * ObjectMapper from the GuiceInjector singleton.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Provider
@Priority(Priorities.USER - 100)
public class CustomObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private final ObjectMapper objectMapper;

  /**
   * No-arg constructor required by JAX-RS.
   *
   * <p>Obtains the ObjectMapper from the GuiceInjector singleton.
   */
  public CustomObjectMapperProvider() {
    this.objectMapper = GuiceInjector.getInstance(ObjectMapper.class);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}
