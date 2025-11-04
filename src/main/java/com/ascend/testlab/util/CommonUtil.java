package com.ascend.testlab.util;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

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
   * Validate the project key.
   *
   * @param projectKey the project key to validate
   * @throws com.dream11.rest.exception.RestException if the project key is blank
   */
  public static void validateProjectKey(String projectKey) {
    if (StringUtils.isBlank(projectKey))
      throw ExceptionUtil.getException(ErrorEnum.INVALID_PROJECT_KEY);
  }
}
