package com.ascend.testlab.allocation.filter;

import com.ascend.testlab.allocation.filter.experimentFilter.CohortFilter;
import com.ascend.testlab.allocation.filter.experimentFilter.CustomAttributesFilter;
import com.ascend.testlab.allocation.filter.experimentFilter.ExperimentFilter;
import com.ascend.testlab.allocation.filter.experimentFilter.ExposureFilter;
import com.ascend.testlab.allocation.filter.experimentFilter.UnassignedExperimentFilter;
import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.dto.request.Attributes;
import com.ascend.testlab.dto.response.UserExperimentMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for building filter chains using Builder pattern. Creates a chain of filters based
 * on request parameters.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentFilterChainBuilder {

  private ExperimentFilter firstFilter;
  private ExperimentFilter currentFilter;

  public ExperimentFilterChainBuilder unassignedFilter(List<UserExperimentMap> userAssignments) {
    addFilter(new UnassignedExperimentFilter(userAssignments));
    return this;
  }

  public ExperimentFilterChainBuilder cohortFilter(List<String> cohorts) {
    addFilter(new CohortFilter(cohorts));
    return this;
  }

  public ExperimentFilterChainBuilder ruleAttributesFilter(Attributes attributes) {
    if (Objects.nonNull(attributes)) {
      addFilter(new CustomAttributesFilter(attributes));
    }
    return this;
  }

  public ExperimentFilterChainBuilder exposureFilter() {
    addFilter(new ExposureFilter());
    return this;
  }

  public static ExperimentFilter buildFromRequest(
      AllocationRequest request, List<UserExperimentMap> userAssignments, List<String> cohorts) {

    ExperimentFilterChainBuilder builder = new ExperimentFilterChainBuilder();

    builder.unassignedFilter(userAssignments);
    builder.cohortFilter(cohorts != null ? cohorts : new ArrayList<>());

    if (Objects.nonNull(request) && Objects.nonNull(request.getAttributes())) {
      Attributes attributes = request.getAttributes();
      builder.ruleAttributesFilter(attributes);
    }

    builder.exposureFilter();

    return builder.build();
  }

  public ExperimentFilter build() {
    if (Objects.isNull(firstFilter)) {
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
    if (Objects.isNull(firstFilter)) {
      firstFilter = filter;
      currentFilter = filter;
    } else {
      currentFilter.setNext(filter);
      currentFilter = filter;
    }
  }
}
