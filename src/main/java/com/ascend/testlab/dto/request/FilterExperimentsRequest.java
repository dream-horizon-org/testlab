package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class FilterExperimentsRequest {

  private List<ExperimentStatus> status;
  private List<String> owner;
  private String name;
  private List<ExperimentType> type;
  private List<String> tag;
  private Integer limit;
  private Integer page;

  // Constants for validation
  private static final int DEFAULT_LIMIT = 20;

  public boolean hasStatusFilter() {
    return this.getStatus() != null && !this.getStatus().isEmpty();
  }

  public boolean hasOwnerFilter() {
    return this.getOwner() != null && !this.getOwner().isEmpty();
  }

  public boolean hasNameFilter() {
    return this.getName() != null && !this.getName().trim().isEmpty();
  }

  public boolean hasTypeFilter() {
    return this.getType() != null && !this.getType().isEmpty();
  }

  public boolean hasTagFilter() {
    return this.getTag() != null && !this.getTag().isEmpty();
  }

  public void buildRequest(
      String status,
      String tag,
      String owner,
      String name,
      String type,
      Integer limit,
      Integer page) {

    // Convert status string to ExperimentStatus enum list
    validateAndSetStatus(status);

    // Convert type string to ExperimentType enum list
    validateAndSetType(type);

    // Handle string parameters
    validateAndSetTag(tag);

    if (owner != null && !owner.isEmpty()) {
      this.setOwner(Arrays.stream(owner.split(",")).map(String::trim).toList());
    }
    if (name != null && !name.isEmpty()) {
      this.setName(name.trim());
    }

    // Handle pagination parameters with validation
    if (limit != null) {
      if (limit <= 0) {
        throw new RestException(ErrorEnum.INVALID_PAGE_LIMIT);
      }
      this.setLimit(limit);
    } else {
      this.setLimit(DEFAULT_LIMIT);
    }

    if (page != null && page <= 0) {
      throw new RestException(ErrorEnum.INVALID_PAGE_NUMBER);
    }
    this.setPage(page);
  }

  private void validateAndSetStatus(String status) {
    if (status != null && !status.isEmpty()) {
      try {
        List<ExperimentStatus> statusList =
            Arrays.stream(status.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(ExperimentStatus::valueOf)
                .toList();
        this.setStatus(statusList);
      } catch (Exception e) {
        log.warn(
            "Invalid status value provided: {}. Valid values are: {}",
            status,
            Arrays.toString(ExperimentStatus.values()));
        throw new RestException(ErrorEnum.VALID_EXPERIMENT_STATUS_FAILED);
      }
    }
  }

  private void validateAndSetType(String type) {
    if (type != null && !type.isEmpty()) {
      try {
        List<ExperimentType> typeList =
            Arrays.stream(type.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(s -> ExperimentType.valueOf(s.replace("/", "_")))
                .toList();
        this.setType(typeList);
      } catch (Exception e) {
        log.warn(
            "Invalid type value provided: {}. Valid values are: {}",
            type,
            Arrays.toString(ExperimentType.values()));
        throw new RestException(ErrorEnum.VALID_EXPERIMENT_TYPE_FAILED);
      }
    }
  }

  private void validateAndSetTag(String tag) {
    if (tag != null && !tag.isEmpty()) {
      this.setTag(Arrays.stream(tag.split(",")).map(String::trim).toList());
    }
  }
}
