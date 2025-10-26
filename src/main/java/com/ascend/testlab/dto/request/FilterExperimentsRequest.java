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
  private static final int MAX_LIMIT = 100;
  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_PAGE = 1000;

  public void buildRequest(
      String status, String tag, String owner, String name, String type, Integer limit, Integer page) {

    // Convert status string to ExperimentStatus enum list
    validateAndSetStatus(status);
    
    // Convert type string to ExperimentType enum list
    validateAndSetType(type);
    
    // Handle string parameters
    validateAndSetTag(tag);

    if (owner != null && !owner.isEmpty()) {
      this.setOwner(Arrays.stream(owner.split(","))
          .map(String::trim)
          .toList());
    }
    if (name != null && !name.isEmpty()) {
      this.setName(name.trim());
    }
    
    // Handle pagination parameters with validation
    if (limit != null) {
      if (limit <= 0) {
        throw new RestException(ErrorEnum.INVALID_PAGE_LIMIT);
      }
      if (limit > MAX_LIMIT) {
        log.warn("Limit {} exceeds maximum {}, using default limit", limit, MAX_LIMIT);
        this.setLimit(MAX_LIMIT);
      } else {
        this.setLimit(limit);
      }
    } else {
      this.setLimit(DEFAULT_LIMIT);
    }
    
    if (page != null && page <= 0) {
      throw new RestException(ErrorEnum.INVALID_PAGE_NUMBER);
    }
    if (page != null && page > MAX_PAGE) {
      log.warn("Page {} exceeds maximum {}, using maximum page", page, MAX_PAGE);
      this.setPage(MAX_PAGE);
    } else {
      this.setPage(page);
    }
  }

  public Integer getSlimit() {
    return this.getLimit();
  }

  private void validateAndSetStatus(String status) {
      if (status != null && !status.isEmpty()) {
          try {
              List<ExperimentStatus> statusList = Arrays.stream(status.split(","))
                      .map(String::trim)
                      .map(String::toUpperCase)
                      .map(ExperimentStatus::valueOf)
                      .toList();
              this.setStatus(statusList);
          } catch (Exception e) {
              log.warn("Invalid status value provided: {}. Valid values are: {}", status, Arrays.toString(ExperimentStatus.values()));
              throw new RestException(ErrorEnum.VALID_EXPERIMENT_STATUS_FAILED);
          }
      }
  }

  private void validateAndSetType(String type) {
      if (type != null && !type.isEmpty()) {
          try {
              List<ExperimentType> typeList = Arrays.stream(type.split(","))
                      .map(String::trim)
                      .map(String::toUpperCase)
                      .map(ExperimentType::valueOf)
                      .toList();
              this.setType(typeList);
          } catch (Exception e) {
              log.warn("Invalid type value provided: {}. Valid values are: {}", type, Arrays.toString(ExperimentType.values()));
              throw new RestException(ErrorEnum.VALID_EXPERIMENT_TYPE_FAILED);
          }
      }
  }

  private void validateAndSetTag(String tag) {
      if (tag != null && !tag.isEmpty()) {
          this.setTag(Arrays.stream(tag.split(","))
                  .map(String::trim)
                  .toList());
      }
  }
}
