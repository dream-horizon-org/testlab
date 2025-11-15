package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import java.util.List;
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

  public static String getSetName(String set, String projectKey) {
    return set + Constants.COLON + projectKey;
  }

  /**
   * Formats comma-separated values for use in a SQL IN clause. Trims whitespace, filters empty
   * values, and wraps each value in single quotes.
   *
   * @param values comma-separated string of values
   * @return formatted string for SQL IN clause (e.g., "'value1', 'value2'")
   */
  public static String formatValuesForInClause(String values) {
    return separateCommaSeparatedString(values).stream()
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
}
