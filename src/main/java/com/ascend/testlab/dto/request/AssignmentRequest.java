package com.ascend.testlab.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Request object for experiment assignment.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
  private List<String> experiments;
  private Attributes attributes;
  private String guestId;
  private String userId;

  public void validate() {
    if (StringUtils.isBlank(guestId) || StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("guestId and userId cannot be blank");
    }
  }
}
