package com.ascend.testlab.util.filter;

import com.ascend.testlab.entity.Experiment;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filters experiments based on user cohort matching Experiments with cohort restrictions must match
 * at least one user cohort
 */
@Slf4j
@RequiredArgsConstructor
public class CohortFilter extends AbstractExperimentFilter {

  private final List<String> userCohorts;

  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    if (userCohorts == null || userCohorts.isEmpty()) {
      log.debug("No user cohorts provided, filtering out cohort-restricted experiments");

      return experiments.stream()
          .filter(
              exp ->
                  exp.getRuleAttributes() == null
                      || exp.getRuleAttributes().getCohorts() == null
                      || exp.getRuleAttributes().getCohorts().isEmpty())
          .collect(Collectors.toList());
    }

    return experiments.stream()
        .filter(
            exp -> {
              if (exp.getRuleAttributes() == null
                  || exp.getRuleAttributes().getCohorts() == null
                  || exp.getRuleAttributes().getCohorts().isEmpty()) {
                return true;
              }

              boolean hasMatch =
                  !Collections.disjoint(exp.getRuleAttributes().getCohorts(), userCohorts);
              if (!hasMatch) {
                log.trace(
                    "Experiment {} filtered out - user cohorts {} do not match required {}",
                    exp.getExperimentId(),
                    userCohorts,
                    exp.getRuleAttributes().getCohorts());
              }
              return hasMatch;
            })
        .collect(Collectors.toList());
  }
}
