package com.ascend.testlab.util.filter;

import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.dto.response.UserExperimentMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for building filter chains using Builder pattern Creates a chain of filters based
 * on request parameters
 */
@Slf4j
public class ExperimentFilterChainBuilder {

  private ExperimentFilter firstFilter;
  private ExperimentFilter currentFilter;

  public ExperimentFilterChainBuilder withUnassignedFilter(
      List<UserExperimentMap> userAssignments) {
    addFilter(new UnassignedExperimentFilter(userAssignments));
    return this;
  }

  public ExperimentFilterChainBuilder withCohortFilter(List<String> cohorts) {
    addFilter(new CohortFilter(cohorts));
    return this;
  }

  public ExperimentFilterChainBuilder withCustomAttributesFilter(
      java.util.Map<String, Object> customAttributes) {
    if (customAttributes != null && !customAttributes.isEmpty()) {
      addFilter(new CustomAttributesFilter(customAttributes));
    }
    return this;
  }

  public static ExperimentFilter buildFromRequest(
      AssignmentRequest request, List<UserExperimentMap> userAssignments) {

    ExperimentFilterChainBuilder builder = new ExperimentFilterChainBuilder();

    builder.withUnassignedFilter(userAssignments);

    if (request.getAttributes() != null) {
      if (request.getAttributes().getCohorts() != null) {
        builder.withCohortFilter(request.getAttributes().getCohorts());
      }

      if (request.getAttributes().getCustomAttributes() != null
          && !request.getAttributes().getCustomAttributes().isEmpty()) {
        builder.withCustomAttributesFilter(request.getAttributes().getCustomAttributes());
      }
    }

    return builder.build();
  }

  public ExperimentFilter build() {
    if (firstFilter == null) {
      log.warn("No filters added to chain, returning pass-through filter");
      return new PassThroughFilter();
    }
    return firstFilter;
  }

  private static class PassThroughFilter implements ExperimentFilter {
    @Override
    public List<com.ascend.testlab.entity.Experiment> filter(
        List<com.ascend.testlab.entity.Experiment> experiments) {
      return experiments;
    }

    @Override
    public ExperimentFilter setNext(ExperimentFilter nextFilter) {
      return nextFilter;
    }
  }

  private void addFilter(ExperimentFilter filter) {
    if (firstFilter == null) {
      firstFilter = filter;
      currentFilter = filter;
    } else {
      currentFilter.setNext(filter);
      currentFilter = filter;
    }
  }
}
