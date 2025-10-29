package com.ascend.testlab.util.filter;

import com.ascend.testlab.entity.Experiment;
import java.util.List;

/**
 * Filter interface for experiment filtering using Chain of Responsibility pattern Each filter can
 * decide whether to pass an experiment to the next filter in the chain
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
