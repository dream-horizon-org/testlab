package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Validates rule attributes constraints.
 *
 * <ul>
 *   <li>Rule attributes list cannot be empty (only in DRAFT mode)
 *   <li>Rules cannot be removed in LIVE or PAUSED mode
 *   <li>Conditions cannot be removed in LIVE or PAUSED mode
 *   <li>For existing conditions: only value can change in LIVE/PAUSED, operandDataType cannot
 *       change
 *   <li>Operand is the differentiator for conditions
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class RuleAttributesValidationRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return request.getRuleAttributes() != null;
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    List<RuleAttributes> requestRules = request.getRuleAttributes();
    ExperimentStatus status = existing.getStatus();

    // Check if rule_attributes is empty - only in DRAFT mode
    if (requestRules.isEmpty() && status == ExperimentStatus.DRAFT) {
      throw new RestException(ErrorEnum.RULE_ATTRIBUTES_CANNOT_BE_EMPTY);
    }

    List<RuleAttributes> existingRules = existing.getRuleAttributes();

    if (existingRules == null || existingRules.isEmpty()) {
      return;
    }

    Map<String, RuleAttributes> existingRulesMap = new HashMap<>();
    for (RuleAttributes rule : existingRules) {
      existingRulesMap.put(rule.getName(), rule);
    }

    // Check for rule removal in LIVE/PAUSED
    if (status != ExperimentStatus.DRAFT) {
      validateNoRuleRemoval(existingRulesMap, requestRules);
    }

    // Validate conditions for each existing rule
    for (RuleAttributes requestRule : requestRules) {
      RuleAttributes existingRule = existingRulesMap.get(requestRule.getName());

      if (existingRule != null) {
        validateExistingRuleConditions(status, existingRule, requestRule);
      }
    }
  }

  /**
   * Validates that no existing rules are removed in LIVE/PAUSED mode.
   *
   * @param existingRulesMap map of existing rules by name
   * @param requestRules list of rules in request
   * @throws RestException if any rule is removed
   */
  private void validateNoRuleRemoval(
      Map<String, RuleAttributes> existingRulesMap, List<RuleAttributes> requestRules) {

    Set<String> requestRuleNames = new HashSet<>();
    for (RuleAttributes rule : requestRules) {
      requestRuleNames.add(rule.getName());
    }

    for (String existingRuleName : existingRulesMap.keySet()) {
      if (!requestRuleNames.contains(existingRuleName)) {
        throw new RestException(ErrorEnum.RULE_REMOVAL_NOT_ALLOWED);
      }
    }
  }

  /**
   * Validates conditions for an existing rule:
   *
   * <ul>
   *   <li>Conditions cannot be removed in LIVE or PAUSED mode
   *   <li>For existing conditions (identified by operand): operandDataType cannot change
   *   <li>In LIVE/PAUSED: only value can change (operator cannot change)
   * </ul>
   */
  private void validateExistingRuleConditions(
      ExperimentStatus status, RuleAttributes existingRule, RuleAttributes requestRule) {

    List<RuleAttributes.Condition> existingConditions = existingRule.getConditions();
    List<RuleAttributes.Condition> requestConditions = requestRule.getConditions();

    if (existingConditions == null || existingConditions.isEmpty()) {
      return; // No existing conditions to validate against
    }

    if (requestConditions == null || requestConditions.isEmpty()) {
      // All conditions being removed - only allowed in DRAFT
      if (status != ExperimentStatus.DRAFT) {
        throw new RestException(ErrorEnum.CONDITION_REMOVAL_NOT_ALLOWED);
      }
      return;
    }

    // Build map of existing conditions by operand (operand is the identifier)
    Map<String, RuleAttributes.Condition> existingConditionsMap = new HashMap<>();
    for (RuleAttributes.Condition condition : existingConditions) {
      existingConditionsMap.put(condition.getOperand(), condition);
    }

    // Build set of request operands
    Set<String> requestOperands = new HashSet<>();
    for (RuleAttributes.Condition condition : requestConditions) {
      requestOperands.add(condition.getOperand());
    }

    // Check for removed conditions - only allowed in DRAFT
    if (status != ExperimentStatus.DRAFT) {
      for (String existingOperand : existingConditionsMap.keySet()) {
        if (!requestOperands.contains(existingOperand)) {
          throw new RestException(ErrorEnum.CONDITION_REMOVAL_NOT_ALLOWED);
        }
      }
    }

    // Validate each condition
    for (RuleAttributes.Condition requestCondition : requestConditions) {
      RuleAttributes.Condition existingCondition =
          existingConditionsMap.get(requestCondition.getOperand());

      if (existingCondition != null) {
        // Existing condition - validate operandDataType not changed
        if (!Objects.equals(
            existingCondition.getOperandDataType(), requestCondition.getOperandDataType())) {
          throw new RestException(ErrorEnum.RULE_CONDITION_DATA_TYPE_CHANGED);
        }

        // In LIVE/PAUSED: operator cannot change, only value can change
        if (status != ExperimentStatus.DRAFT) {
          if (!Objects.equals(existingCondition.getOperator(), requestCondition.getOperator())) {
            throw new RestException(ErrorEnum.CONDITION_OPERATOR_CHANGE_NOT_ALLOWED);
          }
        }
      }
    }
  }
}
