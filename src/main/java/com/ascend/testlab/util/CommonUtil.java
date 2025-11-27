package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.injection.GuiceInjector;
import com.dream11.rest.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for common utilities.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
@Slf4j
public final class CommonUtil {

  /**
   * Get the number of cores available.
   *
   * @return the number of cores available
   */
  public static int getNumberOfCores() {
    return CpuCoreSensor.availableProcessors();
  }

  /**
   * Constructs a qualified set name by combining a set name with a project key.
   *
   * @param set the base set name
   * @param projectKey the project key to append
   * @return the qualified set name in the format "set:projectKey"
   */
  public static String getSetName(String set, String projectKey) {
    return set + Constants.UNDER_SCORE + projectKey;
  }

  /**
   * Formats comma-separated values for use in a SQL IN clause. Trims whitespace, filters empty
   * values, and wraps each value in single quotes.
   *
   * @param values list of values
   * @param <T> type of values
   * @return formatted string for SQL IN clause (e.g., "'value1', 'value2'")
   */
  public static <T> String formatValuesForInClause(List<T> values) {
    return values.stream()
        .map(Objects::toString)
        .filter(s -> !s.isEmpty())
        .map(s -> Constants.APOSTROPHE + s + Constants.APOSTROPHE)
        .collect(Collectors.joining(Constants.COMMA));
  }

  /**
   * Formats comma-separated values for use in a SQL IN clause. Trims whitespace, and return list of
   * string
   *
   * @param values comma-separated string of values
   * @return list of string
   */
  public static List<String> separateCommaSeparatedString(String values) {
    return Stream.of(values.split(Constants.COMMA)).map(String::trim).toList();
  }

  /**
   * Creates a function to map HTTP response body to a Java object of the specified class type.
   * Deserializes the JSON response body using Jackson ObjectMapper.
   *
   * @param <T> the type of the target object
   * @param clazz the class type to deserialize the response into
   * @return a function that maps HttpResponse to the specified type
   * @throws com.dream11.rest.exception.RestException if JSON parsing fails
   */
  public static <T> Function<HttpResponse<Buffer>, T> mapResponse(Class<T> clazz) {
    return response -> {
      try {
        ObjectMapper objectMapper = GuiceInjector.getInstance(ObjectMapper.class);
        return objectMapper.readValue(response.bodyAsString(), clazz);
      } catch (Exception ex) {
        log.error("Json Parsing Failed for [{}]:{}, ", clazz, response.bodyAsString(), ex);
        throw ExceptionUtil.getException(ErrorEnum.UPSTREAM_PARSING_ERROR);
      }
    };
  }

  /**
   * Generates a standardized experiment key by replacing spaces and hyphens with underscores and
   * converting to lowercase.
   *
   * @param experimentName the original experiment name
   * @return the standardized experiment key
   */
  public static String getExperimentKey(String experimentName) {
    return experimentName.replaceAll("[\\s-]+", Constants.UNDER_SCORE).toLowerCase();
  }

  /**
   * Calculates the offset for pagination based on page number and limit.
   *
   * @param page the page number (1-based)
   * @param limit the number of items per page
   * @return the offset value to use in SQL queries
   */
  public static int calculateOffset(int page, int limit) {
    return (page - 1) * limit;
  }
}
