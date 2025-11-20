package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

/**
 * Utility class for common utilities.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
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
