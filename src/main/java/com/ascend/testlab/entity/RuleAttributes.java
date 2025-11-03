package com.ascend.testlab.entity;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleAttributes {
  private List<String> cohorts;
  private Map<String, Object> customAttributes;
}
