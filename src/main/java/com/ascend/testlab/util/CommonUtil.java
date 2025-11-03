package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
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

  public static void validateProjectKey(String projectKey) {
    if (StringUtils.isBlank(projectKey))
      throw ExceptionUtil.getException(ErrorEnum.INVALID_PROJECT_ID);
  }

}
