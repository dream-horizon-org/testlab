package com.ascend.testlab.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExperimentMap {
  private UUID experimentId;
  private String experimentName;
  private String variant;
  private String status;
  private Map<String, Object> variables;
  private Boolean isStatic;
  private Boolean isExclusive;
  private List<String> entities;
  private String apiPath;
  private Long assignedAt;
}
