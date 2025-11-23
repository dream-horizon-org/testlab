package com.ascend.testlab.allocation.filter.impl;

import com.ascend.testlab.allocation.filter.AbstractExperimentFilter;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Filters experiments based on user cohort matching. Experiments with cohort restrictions must
 * match at least one user cohort.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see AbstractExperimentFilter
 */
@Slf4j
public class CohortFilter extends AbstractExperimentFilter {

  /** List of user cohorts for filtering */
  private final List<String> userCohorts;

  /**
   * Constructs a CohortFilter with the specified user cohorts.
   *
   * @param userCohorts list of user cohorts
   */
  public CohortFilter(List<String> userCohorts) {
    this.userCohorts = userCohorts;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Filters experiments based on user cohort matching. Experiments with cohort restrictions must
   * match at least one user cohort.
   *
   * @param experiments list of experiments to filter
   * @return
   */
  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    if (CollectionUtils.isEmpty(userCohorts)) {
      log.debug("No user cohorts provided, filtering out cohort-restricted experiments");

      return experiments.stream().filter(exp -> CollectionUtils.isEmpty(exp.getCohorts())).toList();
    }

    return experiments.stream()
        .filter(
            exp -> {
              if (CollectionUtils.isEmpty(exp.getCohorts())) {
                return true;
              }

              boolean hasMatch = !Collections.disjoint(exp.getCohorts(), userCohorts);

              String traceMessage =
                  hasMatch
                      ? "Experiment {} passed cohort filter - user cohorts {} match required {}"
                      : "Experiment {} filtered out - user cohorts {} do not match required {}";
              log.trace(traceMessage, exp.getExperimentId(), userCohorts, exp.getCohorts());

              return hasMatch;
            })
        .toList();
  }
}
