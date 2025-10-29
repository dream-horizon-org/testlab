package com.ascend.testlab.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
  private String variantName;
  private Integer percentage;
  private Map<String, Object> variables;
  private Long currentCount;
}
