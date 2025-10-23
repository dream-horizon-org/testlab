package com.ascend.testlab.util;

import io.vertx.core.impl.cpu.CpuCoreSensor;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class CommonUtil {

  public static int getNumberOfCores() {
    return CpuCoreSensor.availableProcessors();
  }
}
