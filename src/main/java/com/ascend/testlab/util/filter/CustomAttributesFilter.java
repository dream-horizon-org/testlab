package com.ascend.testlab.util.filter;

import com.ascend.testlab.entity.Experiment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filters experiments based on custom attributes matching Experiments with custom attribute
 * restrictions must match all user custom attributes
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAttributesFilter extends AbstractExperimentFilter {

  private final Map<String, Object> userCustomAttributes;

  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    if (userCustomAttributes == null || userCustomAttributes.isEmpty()) {
      log.debug(
          "No user custom attributes provided, filtering out custom attribute-restricted experiments");
      return experiments.stream()
          .filter(
              exp ->
                  exp.getRuleAttributes() == null
                      || exp.getRuleAttributes().getCustomAttributes() == null
                      || exp.getRuleAttributes().getCustomAttributes().isEmpty())
          .collect(Collectors.toList());
    }

    return experiments.stream()
        .filter(
            exp -> {
              if (exp.getRuleAttributes() == null
                  || exp.getRuleAttributes().getCustomAttributes() == null
                  || exp.getRuleAttributes().getCustomAttributes().isEmpty()) {

                return true;
              }

              Map<String, Object> experimentAttributes =
                  exp.getRuleAttributes().getCustomAttributes();
              boolean allMatch =
                  experimentAttributes.entrySet().stream()
                      .allMatch(
                          entry -> {
                            String key = entry.getKey();
                            Object requiredValue = entry.getValue();
                            Object userValue = userCustomAttributes.get(key);

                            boolean matches = userValue != null && userValue.equals(requiredValue);
                            if (!matches) {
                              log.trace(
                                  "Experiment {} attribute mismatch - key: {}, required: {}, user: {}",
                                  exp.getExperimentId(),
                                  key,
                                  requiredValue,
                                  userValue);
                            }
                            return matches;
                          });

              if (!allMatch) {
                log.trace(
                    "Experiment {} filtered out - custom attributes do not match requirements",
                    exp.getExperimentId());
              }

              return allMatch;
            })
        .collect(Collectors.toList());
  }
}
