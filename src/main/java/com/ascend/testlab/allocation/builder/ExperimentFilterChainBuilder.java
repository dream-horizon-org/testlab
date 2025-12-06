package com.ascend.testlab.allocation.builder;

import com.ascend.testlab.allocation.filter.ExperimentFilter;
import com.ascend.testlab.allocation.filter.impl.AttributesFilter;
import com.ascend.testlab.allocation.filter.impl.CohortFilter;
import com.ascend.testlab.allocation.filter.impl.ExposureFilter;
import com.ascend.testlab.allocation.filter.impl.UnassignedExperimentFilter;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.AllocationRequest;
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

  /**
   * Builds the filter chain from the allocation request.
   *
   * @param request the allocation request
   * @param userAssignments the list of user assignments
   * @param cohorts the list of cohorts
   * @return the head of the filter chain
   */
  public static ExperimentFilter buildFromRequest(
      AllocationRequest request, List<UserExperimentMap> userAssignments, List<String> cohorts) {

    return new ExperimentFilterChainBuilder()
        .addFilter(new UnassignedExperimentFilter(userAssignments))
        .addFilter(new CohortFilter(cohorts))
        .addFilter(new AttributesFilter(request.getAttributes()))
        .addFilter(new ExposureFilter())
        .build();
  }

  /**
   * Adds a filter to the chain.
   *
   * @param filter the filter to add
   * @return the builder instance
   */
  public ExperimentFilterChainBuilder addFilter(ExperimentFilter filter) {
    if (Objects.isNull(firstFilter)) {
      firstFilter = filter;
    } else {
      currentFilter.setNext(filter);
    }
    currentFilter = filter;
    return this;
  }

  /**
   * Builds the filter chain.
   *
   * @return the head of the filter chain
   */
  public ExperimentFilter build() {
    if (Objects.isNull(firstFilter)) {
      log.warn("No filters added to chain, returning pass-through filter");
      return new PassThroughFilter();
    }
    return firstFilter;
  }

  private static class PassThroughFilter implements ExperimentFilter {
    @Override
    public List<Experiment> filter(List<Experiment> experiments) {
      return experiments;
    }

    @Override
    public ExperimentFilter setNext(ExperimentFilter nextFilter) {
      return nextFilter;
    }
  }
}
