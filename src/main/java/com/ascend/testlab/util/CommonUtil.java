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

  public static void validateProjectId(String projectId) {
    Boolean isValidProjectId = StringUtils.isNotBlank(projectId) && isValidUUID(projectId);
    if (Boolean.FALSE.equals(isValidProjectId))
      throw ExceptionUtil.getException(ErrorEnum.INVALID_PROJECT_ID);
  }

  public static void validateExperimentId(String experimentId) {
    if (experimentId == null || experimentId.trim().isEmpty()) {
      throw ExceptionUtil.getException(ErrorEnum.INVALID_EXPERIMENT_ID);
    }
    if (!isValidUUID(experimentId)) {
      throw ExceptionUtil.getException(ErrorEnum.INVALID_EXPERIMENT_ID);
    }
  }

  public static Boolean isValidUUID(String uuidString) {
    return Constants.UUID_REGEX.matcher(uuidString).matches();
  }
}
