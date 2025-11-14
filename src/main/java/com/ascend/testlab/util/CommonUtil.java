package com.ascend.testlab.util;

import io.vertx.core.impl.cpu.CpuCoreSensor;
import java.util.Arrays;
import java.util.stream.Collectors;
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
   * @param values comma-separated string of values
   * @return formatted string for SQL IN clause (e.g., "'value1', 'value2'")
   */
  public static String formatValuesForInClause(String values) {
    return Arrays.stream(values.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> "'" + s + "'")
        .collect(Collectors.joining(", "));
  }
}
