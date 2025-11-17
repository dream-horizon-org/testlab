package com.ascend.testlab.allocation.filter.experimentFilter;

import com.ascend.testlab.allocation.filter.AbstractExperimentFilter;
import com.ascend.testlab.constants.attributes.RelationalOperator;
import com.ascend.testlab.constants.enums.DataTypeEnum;
import com.ascend.testlab.dto.request.Attributes;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.RuleAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filters experiments based on custom attributes matching. Logic: - Across rules: OR (if ANY rule
 * matches, the experiment passes) - Within a rule's conditions: AND (ALL conditions must be true
 * for the rule to match)
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see AbstractExperimentFilter
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAttributesFilter extends AbstractExperimentFilter {

  private final Attributes attributes;

  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    if (Objects.isNull(attributes)) {
      log.debug("No user attributes provided, filtering out attribute-restricted experiments");
      return experiments.stream()
          .filter(
              exp -> Objects.isNull(exp.getRuleAttributes()) || exp.getRuleAttributes().isEmpty())
          .collect(Collectors.toList());
    }

    return experiments.stream()
        .filter(
            exp -> {
              if (Objects.isNull(exp.getRuleAttributes()) || exp.getRuleAttributes().isEmpty()) {
                return true;
              }

              boolean anyConditionMatches =
                  exp.getRuleAttributes().stream().anyMatch(this::evaluateRuleAttribute);

              if (!anyConditionMatches) {
                log.trace(
                    "Experiment {} filtered out - none of the {} rule attribute conditions matched",
                    exp.getExperimentId(),
                    exp.getRuleAttributes().size());
              } else {
                log.debug(
                    "Experiment {} passed filter - at least one rule attribute condition matched",
                    exp.getExperimentId());
              }

              return anyConditionMatches;
            })
        .collect(Collectors.toList());
  }

  /**
   * Evaluates a single RuleAttributes against user attributes. All conditions within the rule must
   * be true (AND logic).
   *
   * @param ruleAttributes the rule attribute to evaluate
   * @return true if ALL conditions in the rule evaluate to true, false otherwise
   */
  private boolean evaluateRuleAttribute(RuleAttributes ruleAttributes) {
    try {
      if (Objects.isNull(ruleAttributes.getConditions())
          || ruleAttributes.getConditions().isEmpty()) {
        log.trace("No conditions in rule: {}", ruleAttributes.getName());
        return false;
      }

      boolean allConditionsMatch =
          ruleAttributes.getConditions().stream()
              .allMatch(condition -> evaluateCondition(condition, ruleAttributes.getName()));

      log.debug(
          "Rule '{}' evaluation result: {} (total conditions: {})",
          ruleAttributes.getName(),
          allConditionsMatch,
          ruleAttributes.getConditions().size());

      return allConditionsMatch;
    } catch (Exception e) {
      log.error("Error evaluating rule attribute: {}", ruleAttributes.getName(), e);
      return false;
    }
  }

  /**
   * Evaluates a single condition against user attributes.
   *
   * @param condition the condition to evaluate
   * @param ruleName the name of the rule (for logging)
   * @return true if the condition evaluates to true, false otherwise
   */
  private boolean evaluateCondition(RuleAttributes.Condition condition, String ruleName) {
    try {
      String operandName = condition.getOperand();
      String userAttributeValue = getUserAttributeValue(operandName);

      if (Objects.isNull(userAttributeValue)) {
        log.trace(
            "User attribute value is null for operand: {} in rule: {}", operandName, ruleName);
        return false;
      }

      String expectedValue = condition.getValue();

      RelationalOperator operator = RelationalOperator.getOperator(condition.getOperator());

      DataTypeEnum dataType = DataTypeEnum.getDataType(condition.getOperandDataType());
      boolean result =
          dataType
              .getEvaluation()
              .evaluate(userAttributeValue, expectedValue, operator, operandName);

      log.debug(
          "Rule '{}' - Evaluated condition: {} {} {} = {}",
          ruleName,
          operandName,
          operator.getName(),
          expectedValue,
          result);

      return result;
    } catch (Exception e) {
      log.error("Error evaluating condition in rule '{}': {}", ruleName, condition, e);
      return false;
    }
  }

  /**
   * Extracts the user attribute value based on operand name.
   *
   * @param operandName the operand name (e.g., "app_version", "platform")
   * @return the user attribute value as string, or null if not found
   */
  private String getUserAttributeValue(String operandName) {
    if (attributes == null) {
      return null;
    }

    try {

      return switch (operandName) {
        case "app_version" -> attributes.getAppVersion();
        case "platform" -> attributes.getPlatform();
        case "os_version" -> attributes.getOsVersion();
        case "build_version" -> attributes.getBuildVersion();
        case "build_number" -> attributes.getBuildNumber();
        case "model" -> attributes.getModel();
        case "device" -> attributes.getDevice();
        case "app_name" -> attributes.getAppName();
        default -> {
          log.warn("Unknown operand: {}", operandName);
          yield null;
        }
      };
    } catch (Exception e) {
      log.error("Error extracting user attribute for operand: {}", operandName, e);
      return null;
    }
  }
}
