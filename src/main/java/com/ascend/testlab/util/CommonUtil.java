package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.datadog.DDConstants;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public final class CommonUtil {

  public static int getNumberOfCores() {
    return CpuCoreSensor.availableProcessors();
  }

  public static String getCircuitBreakerAspect(String aspect) {
    return DDConstants.CB_METRIC + Constants.SPACE + aspect;
  }

  public static String getCircuitBreakerTag(String circuitBreakerName) {
    return DDConstants.CB_NAME + Constants.COLON + circuitBreakerName;
  }

  public static void validateUser(String userId) {
    if (StringUtils.isBlank(userId)) {
      throw ExceptionUtil.getException(ErrorEnum.MISSING_USER_IDENTIFIER);
    }
  }
}
