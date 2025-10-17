package com.ascend.testlab.util;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ExceptionUtil {

  public static RestException getException(ErrorEnum errorEnum) {
    return new RestException(errorEnum);
  }

  public static RestException getException(ErrorEnum errorEnum, Throwable throwable) {
    return new RestException(errorEnum, throwable);
  }
}
