package com.ascend.testlab.dto.entity.allocation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User attributes for experiment filtering and targeting.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attributes {
  @JsonProperty("build_version")
  private String buildVersion;

  private String model;
  private String device;

  @JsonProperty("app_name")
  private String appName;

  private String platform;

  @JsonProperty("os_version")
  private String osVersion;

  @JsonProperty("app_version")
  private String appVersion;

  @JsonProperty("build_number")
  private String buildNumber;
}
