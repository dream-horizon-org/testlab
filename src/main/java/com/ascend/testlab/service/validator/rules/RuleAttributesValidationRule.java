package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Validates rule attributes constraints.
 *
 * <ul>
 *   <li>New rules can be added
 *   <li>For existing rules, new conditions can be added
 *   <li>For existing conditions, value and operator can change, but operandDataType cannot
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
    return request.getRuleAttributes() != null && !request.getRuleAttributes().isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    List<RuleAttributes> requestRules = request.getRuleAttributes();
    List<RuleAttributes> existingRules = existing.getRuleAttributes();

    if (existingRules == null || existingRules.isEmpty()) {
      return;
    }

    Map<String, RuleAttributes> existingRulesMap = new HashMap<>();
    for (RuleAttributes rule : existingRules) {
      existingRulesMap.put(rule.getName(), rule);
    }

    for (RuleAttributes requestRule : requestRules) {
      RuleAttributes existingRule = existingRulesMap.get(requestRule.getName());

      if (existingRule != null) {
        validateExistingRuleConditions(existingRule, requestRule);
      }
    }
  }

  /**
   * Validates conditions for an existing rule: - New conditions can be added - For existing
   * conditions (identified by operand): operandDataType cannot change - value and operator CAN be
   * changed
   */
  private void validateExistingRuleConditions(
      RuleAttributes existingRule, RuleAttributes requestRule) {

    List<RuleAttributes.Condition> existingConditions = existingRule.getConditions();
    List<RuleAttributes.Condition> requestConditions = requestRule.getConditions();

    if (requestConditions == null || requestConditions.isEmpty()) {
      return;
    }

    if (existingConditions == null || existingConditions.isEmpty()) {
      return; // All conditions are new, allowed
    }

    // Build map of existing conditions by operand (operand is the identifier)
    Map<String, RuleAttributes.Condition> existingConditionsMap = new HashMap<>();
    for (RuleAttributes.Condition condition : existingConditions) {
      existingConditionsMap.put(condition.getOperand(), condition);
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
        // operator CAN be changed
        // value CAN be changed
      }
      // New condition (operand not in existing) is allowed
    }
  }
}
