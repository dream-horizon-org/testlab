package com.ascend.testlab.allocation.filter;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for experiment filters implementing Chain of Responsibility pattern.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see ExperimentFilter
 */
@Slf4j
public abstract class AbstractExperimentFilter implements ExperimentFilter {

  /** Next filter in the chain */
  protected ExperimentFilter nextFilter;

  /** {@inheritDoc} */
  @Override
  public ExperimentFilter setNext(ExperimentFilter nextFilter) {
    this.nextFilter = nextFilter;
    return nextFilter;
  }

  /** {@inheritDoc} */
  @Override
  public List<Experiment> filter(List<Experiment> experiments) {
    List<Experiment> filtered = applyFilter(experiments);
    log.info(
        "Filter {} reduced experiments from {} to {}",
        this.getClass().getSimpleName(),
        experiments.size(),
        filtered.size());

    if (nextFilter != null) {
      return nextFilter.filter(filtered);
    }
    return filtered;
  }

  /**
   * Applies the specific filter logic
   *
   * @param experiments list of experiments to filter
   * @return filtered list of experiments
   */
  protected abstract List<Experiment> applyFilter(List<Experiment> experiments);
}
