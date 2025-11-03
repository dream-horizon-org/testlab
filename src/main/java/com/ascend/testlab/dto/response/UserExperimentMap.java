package com.ascend.testlab.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// todo
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
  private List<String> entities;
  private Long assignedAt;
}
