package com.ascend.testlab.util.filter.experimentFilter;

import com.ascend.testlab.entity.Experiment;
import java.util.List;

/**
 * Filter interface for experiment filtering using Chain of Responsibility pattern. Each filter can
 * decide whether to pass an experiment to the next filter in the chain.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentFilter {

  /**
   * Filters experiments based on specific criteria
   *
   * @param experiments list of experiments to filter
   * @return filtered list of experiments
   */
  List<Experiment> filter(List<Experiment> experiments);

  /**
   * Sets the next filter in the chain
   *
   * @param nextFilter next filter to execute
   * @return the next filter for method chaining
   */
  ExperimentFilter setNext(ExperimentFilter nextFilter);
}
