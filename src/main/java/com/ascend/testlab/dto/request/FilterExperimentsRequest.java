package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Request DTO for filtering experiments with various criteria. Supports filtering by status, owner,
 * name, type, and tags, along with pagination.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class FilterExperimentsRequest {
  @QueryParam(WebConstants.EXPERIMENT_STATUS)
  private String status;

  @QueryParam(WebConstants.OWNER)
  private String owner;

  @QueryParam(WebConstants.NAME)
  private String name;

  @QueryParam(WebConstants.EXPERIMENT_TYPE)
  private String type;

  @QueryParam(WebConstants.TAG)
  private String tag;

  @QueryParam(WebConstants.LIMIT)
  @DefaultValue(WebConstants.DEFAULT_LIMIT)
  @Positive(message = ErrorMessages.INVALID_LIMIT_VALUE)
  private Integer limit;

  @QueryParam(WebConstants.PAGE)
  @DefaultValue(WebConstants.DEFAULT_PAGE)
  @Positive(message = ErrorMessages.INVALID_PAGE_VALUE)
  private Integer page;

  /**
   * Checks if status filter is present in the request.
   *
   * @return true if status filter is present and not empty, false otherwise
   */
  public boolean hasStatusFilter() {
    return this.status != null && !this.status.trim().isEmpty();
  }

  /**
   * Checks if owner filter is present in the request.
   *
   * @return true if owner filter is present and not empty, false otherwise
   */
  public boolean hasOwnerFilter() {
    return this.owner != null && !this.owner.trim().isEmpty();
  }

  /**
   * Checks if name filter is present in the request.
   *
   * @return true if name filter is present and not empty (after trimming), false otherwise
   */
  public boolean hasNameFilter() {
    return this.name != null && !this.name.trim().isEmpty();
  }

  /**
   * Checks if type filter is present in the request.
   *
   * @return true if type filter is present and not empty, false otherwise
   */
  public boolean hasTypeFilter() {
    return this.type != null && !this.type.trim().isEmpty();
  }

  /**
   * Checks if tag filter is present in the request.
   *
   * @return true if tag filter is present and not empty, false otherwise
   */
  public boolean hasTagFilter() {
    return this.tag != null && !this.tag.trim().isEmpty();
  }

  /**
   * Validates the status field using Bean Validation. Returns true if status is null, empty, or
   * contains valid comma-separated ExperimentStatus enum values.
   *
   * @return true if status is valid, false otherwise
   */
  @AssertTrue(message = ErrorMessages.INVALID_EXPERIMENT_STATUS)
  public boolean isValidStatus() {
    if (status == null || status.trim().isEmpty()) {
      return true; // Status is optional
    }
    try {
      List<String> statusValues = CommonUtil.separateCommaSeparatedString(status);
      for (String statusValue : statusValues) {
        if (!statusValue.isEmpty()) {
          ExperimentStatus.valueOf(statusValue.toUpperCase());
        }
      }
      return true;
    } catch (IllegalArgumentException e) {
      log.warn(
          "Invalid status value provided: {}. Valid values are: {}",
          status,
          Arrays.toString(ExperimentStatus.values()));
      return false;
    }
  }

  /**
   * Validates the type field using Bean Validation. Returns true if type is null, empty, or
   * contains valid comma-separated ExperimentType enum values.
   *
   * @return true if type is valid, false otherwise
   */
  @AssertTrue(message = ErrorMessages.INVALID_EXPERIMENT_TYPE)
  public boolean isValidType() {
    if (type == null || type.trim().isEmpty()) {
      return true; // Type is optional
    }
    try {
      List<String> typeValues = CommonUtil.separateCommaSeparatedString(type);
      for (String typeValue : typeValues) {
        if (!typeValue.isEmpty()) {
          ExperimentType.fromValue(typeValue);
        }
      }
      return true;
    } catch (RestException e) {
      log.warn(
          "Invalid type value provided: {}. Valid values are: {}",
          type,
          Arrays.toString(ExperimentType.values()));
      return false;
    }
  }
}
