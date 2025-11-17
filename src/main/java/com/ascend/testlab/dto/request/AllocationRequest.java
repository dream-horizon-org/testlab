package com.ascend.testlab.dto.request;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Request object for experiment allocation.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequest {
  private List<String> experiments;
  private Attributes attributes;
  private String guestId;
  private String userId;

  public void validate() {
    if (StringUtils.isBlank(guestId) || StringUtils.isBlank(userId)) {
      throw ExceptionUtil.getException(ErrorEnum.MISSING_USER_IDENTIFIER);
    }
    if (experiments.isEmpty()) {
      throw ExceptionUtil.getException(ErrorEnum.INVALID_REQUEST_BODY);
    }
  }
}
