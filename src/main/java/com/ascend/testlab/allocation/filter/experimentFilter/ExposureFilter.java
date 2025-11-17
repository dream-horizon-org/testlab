package com.ascend.testlab.allocation.filter.experimentFilter;

import com.ascend.testlab.allocation.filter.AbstractExperimentFilter;
import com.ascend.testlab.entity.Experiment;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Filters experiments based on exposure percentage. Each experiment is randomly evaluated against
 * its exposure setting to determine if it should be shown to the user.
 *
 * <p>Exposure is a percentage value (0-100) that determines what rate of assignment for users
 * should see the experiment. For example, an exposure of 20 means ~20% of users will be exposed to
 * the experiment at given time.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see AbstractExperimentFilter
 */
@Slf4j
public class ExposureFilter extends AbstractExperimentFilter {

  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    return experiments.stream()
        .filter(
            exp -> {
              boolean shouldExpose = validateExposure(exp);

              if (!shouldExpose) {
                log.trace(
                    "Experiment {} filtered out due to exposure (exposure: {}%)",
                    exp.getExperimentId(), exp.getExposure());
              } else {
                log.debug(
                    "Experiment {} passed exposure filter (exposure: {}%)",
                    exp.getExperimentId(), exp.getExposure());
              }

              return shouldExpose;
            })
        .collect(Collectors.toList());
  }

  /**
   * Validates if the experiment should be exposed based on its exposure percentage.
   *
   * @param experiment the experiment to validate
   * @return true if the experiment should be exposed, false otherwise
   */
  private boolean validateExposure(Experiment experiment) {
    int exposure = experiment.getExposure();

    int exp = ThreadLocalRandom.current().nextInt(0, 100);

    return exp <= exposure && exposure != 0;
  }
}
