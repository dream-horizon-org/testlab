package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.Arrays;
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
  @DefaultValue("20")
  @Positive(message = "Limit must be greater than zero")
  private Integer limit;

  @QueryParam(WebConstants.OFFSET)
  @DefaultValue("1")
  @Positive(message = "Page value must be greater than zero")
  private Integer page;

  /**
   * Validates all filter parameters. This method should be called before using the request to
   * ensure all parameters are valid.
   *
   * @throws RestException if any validation fails
   */
  public void validate() {
    validateStatus(status);
    validateType(type);
  }

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

  private void validateStatus(String status) {
    if (status != null && !status.trim().isEmpty()) {
      try {
        String[] statusValues = status.split(",");
        for (String statusValue : statusValues) {
          String trimmed = statusValue.trim();
          if (!trimmed.isEmpty()) {
            ExperimentStatus.valueOf(trimmed.toUpperCase());
          }
        }
      } catch (IllegalArgumentException e) {
        log.warn(
            "Invalid status value provided: {}. Valid values are: {}",
            status,
            Arrays.toString(ExperimentStatus.values()));
        throw new RestException(ErrorEnum.INVALID_EXPERIMENT_STATUS);
      }
    }
  }

  private void validateType(String type) {
    if (type != null && !type.trim().isEmpty()) {
      try {
        String[] typeValues = type.split(",");
        for (String typeValue : typeValues) {
          String trimmed = typeValue.trim();
          if (!trimmed.isEmpty()) {
            ExperimentType.fromValue(trimmed);
          }
        }
      } catch (RestException e) {
        log.warn(
            "Invalid type value provided: {}. Valid values are: {}",
            type,
            Arrays.toString(ExperimentType.values()));
        throw new RestException(ErrorEnum.INVALID_EXPERIMENT_TYPE);
      }
    }
  }
}
