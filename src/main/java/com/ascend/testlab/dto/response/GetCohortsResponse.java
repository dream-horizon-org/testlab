package com.ascend.testlab.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for get cohorts API.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCohortsResponse {

  private List<String> data;
}
