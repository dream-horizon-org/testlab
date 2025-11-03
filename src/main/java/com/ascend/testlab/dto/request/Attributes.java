package com.ascend.testlab.dto.request;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** User attributes for experiment filtering and targeting */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attributes {
  private String appVersion;
  private String buildVersion;
  private String trait; // User trait for filtering
  private List<String> cohorts; // User cohorts for filtering
  private Map<String, Object> customAttributes; // Additional custom attributes
}
