package com.ascend.testlab.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Custom ObjectMapper provider for Jackson configuration.
 *
 * <p>Configures Jackson to: - Fail on unknown properties - Fail on invalid enum values with clear
 * error messages - Support Java 8 date/time types
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Provider
public class CustomObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private final ObjectMapper objectMapper;

  public CustomObjectMapperProvider() {
    objectMapper = new ObjectMapper();

    // Register Java 8 date/time module
    objectMapper.registerModule(new JavaTimeModule());

    // Configure deserialization
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);

    // Configure serialization
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}
