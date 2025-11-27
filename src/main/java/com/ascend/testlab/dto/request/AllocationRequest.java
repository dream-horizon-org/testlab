package com.ascend.testlab.dto.request;

import com.ascend.testlab.dto.entity.allocation.Attributes;
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
  private List<String> experimentKeys;

  private Attributes attributes;

  private String stableId;

  private String userId;

  /**
   * Validates the allocation request.
   *
   * @throws RuntimeException if the request is invalid
   */
  public void validate() {
    if (StringUtils.isBlank(stableId) && StringUtils.isBlank(userId)) {
      throw ExceptionUtil.getException(ErrorEnum.MISSING_USER_IDENTIFIER);
    }
    if (experimentKeys.isEmpty()) {
      throw ExceptionUtil.getException(ErrorEnum.INVALID_REQUEST_BODY);
    }
  }
}
