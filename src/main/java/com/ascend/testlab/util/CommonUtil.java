package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import io.vertx.core.impl.cpu.CpuCoreSensor;
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
}
