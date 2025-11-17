package com.ascend.testlab.util.filter.experimentFilter;

import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.util.filter.AbstractExperimentFilter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@RequiredArgsConstructor
public class CohortFilter extends AbstractExperimentFilter {

  private final List<String> userCohorts;

  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    if (Objects.isNull(userCohorts) || userCohorts.isEmpty()) {
      log.debug("No user cohorts provided, filtering out cohort-restricted experiments");

      return experiments.stream()
          .filter(exp -> Objects.isNull(exp.getCohorts()) || exp.getCohorts().isEmpty())
          .collect(Collectors.toList());
    }

    return experiments.stream()
        .filter(
            exp -> {
              if (Objects.isNull(exp.getCohorts()) || exp.getCohorts().isEmpty()) {
                return true;
              }

              boolean hasMatch = !Collections.disjoint(exp.getCohorts(), userCohorts);

              if (!hasMatch) {
                log.trace(
                    "Experiment {} filtered out - user cohorts {} do not match required {}",
                    exp.getExperimentId(),
                    userCohorts,
                    exp.getCohorts());
              } else {
                log.debug(
                    "Experiment {} passed cohort filter - user cohorts {} match required {}",
                    exp.getExperimentId(),
                    userCohorts,
                    exp.getCohorts());
              }

              return hasMatch;
            })
        .collect(Collectors.toList());
  }
}
