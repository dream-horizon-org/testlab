package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.datadog.DDConstants;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import lombok.experimental.UtilityClass;

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
  }
