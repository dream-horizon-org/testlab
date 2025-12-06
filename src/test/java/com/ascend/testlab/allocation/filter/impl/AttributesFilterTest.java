package com.ascend.testlab.allocation.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.allocation.Attributes;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AttributesFilter Tests")
class AttributesFilterTest {

  @Test
  @DisplayName("Should return all experiments without rule attributes when user has no attributes")
  void testFilterWithNoUserAttributes() {
    // Arrange
    AttributesFilter filter = new AttributesFilter(null);

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", Collections.emptyList()),
            createExperiment("exp2", createRuleAttributes()),
            createExperiment("exp3", createRuleAttributes()));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals("exp1", result.get(0).getName());
  }

  @Test
  @DisplayName("Should return all experiments when no rule attributes exist")
  void testFilterWithNoRuleAttributesInExperiments() {
    // Arrange
    Attributes userAttributes =
        Attributes.builder().appVersion("1.0.0").platform("iOS").osVersion("14.0").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", Collections.emptyList()),
            createExperiment("exp2", Collections.emptyList()),
            createExperiment("exp3", Collections.emptyList()));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(3, result.size());
  }

  @Test
  @DisplayName("Should filter experiments that match app_version condition")
  void testFilterExperimentsMatchingAppVersion() {
    // Arrange
    Attributes userAttributes = Attributes.builder().appVersion("1.5.0").platform("iOS").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> ruleMatch = createRuleWithCondition("app_version", "STRING", "=", "1.5.0");
    List<RuleAttributes> ruleNoMatch =
        createRuleWithCondition("app_version", "STRING", "=", "2.0.0");

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", ruleMatch),
            createExperiment("exp2", ruleNoMatch),
            createExperiment("exp3", Collections.emptyList()));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp1")));
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp3")));
  }

  @Test
  @DisplayName("Should filter experiments that match platform condition")
  void testFilterExperimentsMatchingPlatform() {
    // Arrange
    Attributes userAttributes =
        Attributes.builder().platform("Android").appVersion("1.0.0").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> ruleMatch = createRuleWithCondition("platform", "STRING", "=", "Android");
    List<RuleAttributes> ruleNoMatch = createRuleWithCondition("platform", "STRING", "=", "iOS");

    List<Experiment> experiments =
        List.of(createExperiment("exp1", ruleMatch), createExperiment("exp2", ruleNoMatch));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals("exp1", result.get(0).getName());
  }

  @Test
  @DisplayName("Should handle multiple conditions with AND logic within a rule")
  void testFilterWithMultipleConditionsInRule() {
    // Arrange
    Attributes userAttributes =
        Attributes.builder().platform("iOS").appVersion("1.5.0").osVersion("14.0").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    // Create rule with multiple conditions (ALL must match)
    List<RuleAttributes.Condition> conditions = new ArrayList<>();
    conditions.add(createCondition("platform", "STRING", "=", "iOS"));
    conditions.add(createCondition("app_version", "STRING", "=", "1.5.0"));

    List<RuleAttributes> ruleAttributes =
        List.of(
            RuleAttributes.builder().name("multi_condition_rule").conditions(conditions).build());

    List<Experiment> experiments = List.of(createExperiment("exp1", ruleAttributes));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should filter out when one condition in AND rule fails")
  void testFilterWithMultipleConditionsOneFailsInRule() {
    // Arrange
    Attributes userAttributes =
        Attributes.builder().platform("iOS").appVersion("1.5.0").osVersion("14.0").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    // Create rule with multiple conditions where one doesn't match
    List<RuleAttributes.Condition> conditions = new ArrayList<>();
    conditions.add(createCondition("platform", "STRING", "=", "iOS"));
    conditions.add(createCondition("app_version", "STRING", "=", "2.0.0")); // This won't match

    List<RuleAttributes> ruleAttributes =
        List.of(
            RuleAttributes.builder().name("multi_condition_rule").conditions(conditions).build());

    List<Experiment> experiments = List.of(createExperiment("exp1", ruleAttributes));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should handle multiple rules with OR logic")
  void testFilterWithMultipleRulesOrLogic() {
    // Arrange
    Attributes userAttributes = Attributes.builder().platform("iOS").appVersion("1.5.0").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    // Create multiple rules (ANY can match)
    List<RuleAttributes> ruleAttributes = new ArrayList<>();
    ruleAttributes.add(
        RuleAttributes.builder()
            .name("rule1")
            .conditions(List.of(createCondition("platform", "STRING", "=", "Android")))
            .build());
    ruleAttributes.add(
        RuleAttributes.builder()
            .name("rule2")
            .conditions(List.of(createCondition("app_version", "STRING", "=", "1.5.0")))
            .build());

    List<Experiment> experiments = List.of(createExperiment("exp1", ruleAttributes));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    // Should match because rule2 matches (OR logic)
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should handle os_version attribute")
  void testFilterWithOsVersion() {
    // Arrange
    Attributes userAttributes = Attributes.builder().osVersion("14.5").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> rule = createRuleWithCondition("os_version", "STRING", "=", "14.5");

    List<Experiment> experiments = List.of(createExperiment("exp1", rule));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should handle build_number attribute")
  void testFilterWithBuildNumber() {
    // Arrange
    Attributes userAttributes = Attributes.builder().buildNumber("456").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> rule = createRuleWithCondition("build_number", "STRING", "=", "456");

    List<Experiment> experiments = List.of(createExperiment("exp1", rule));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should handle model attribute")
  void testFilterWithModel() {
    // Arrange
    Attributes userAttributes = Attributes.builder().model("iPhone 12").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> rule = createRuleWithCondition("model", "STRING", "=", "iPhone 12");

    List<Experiment> experiments = List.of(createExperiment("exp1", rule));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should handle device attribute")
  void testFilterWithDevice() {
    // Arrange
    Attributes userAttributes = Attributes.builder().device("mobile").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> rule = createRuleWithCondition("device", "STRING", "=", "mobile");

    List<Experiment> experiments = List.of(createExperiment("exp1", rule));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should handle app_name attribute")
  void testFilterWithAppName() {
    // Arrange
    Attributes userAttributes = Attributes.builder().appName("TestApp").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> rule = createRuleWithCondition("app_name", "STRING", "=", "TestApp");

    List<Experiment> experiments = List.of(createExperiment("exp1", rule));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should filter out when user attribute value is null")
  void testFilterWhenUserAttributeValueIsNull() {
    // Arrange
    Attributes userAttributes =
        Attributes.builder()
            .platform("iOS")
            // appVersion is null
            .build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> rule = createRuleWithCondition("app_version", "STRING", "=", "1.0.0");

    List<Experiment> experiments = List.of(createExperiment("exp1", rule));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should filter out when rule has no conditions")
  void testFilterWhenRuleHasNoConditions() {
    // Arrange
    Attributes userAttributes = Attributes.builder().platform("iOS").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);

    List<RuleAttributes> ruleAttributes =
        List.of(
            RuleAttributes.builder()
                .name("empty_rule")
                .conditions(Collections.emptyList())
                .build());

    List<Experiment> experiments = List.of(createExperiment("exp1", ruleAttributes));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should handle empty experiments list")
  void testFilterWithEmptyExperimentsList() {
    // Arrange
    Attributes userAttributes = Attributes.builder().platform("iOS").build();

    AttributesFilter filter = new AttributesFilter(userAttributes);
    List<Experiment> experiments = Collections.emptyList();

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should work with chain of filters")
  void testFilterChaining() {
    // Arrange
    Attributes userAttributes = Attributes.builder().platform("iOS").appVersion("1.5.0").build();

    AttributesFilter filter1 = new AttributesFilter(userAttributes);
    AttributesFilter filter2 = new AttributesFilter(userAttributes);

    filter1.setNext(filter2);

    List<RuleAttributes> rule1 = createRuleWithCondition("platform", "STRING", "=", "iOS");

    List<Experiment> experiments =
        List.of(createExperiment("exp1", rule1), createExperiment("exp2", Collections.emptyList()));

    // Act
    List<Experiment> result = filter1.filter(experiments);

    // Assert
    assertEquals(2, result.size());
  }

  // Helper methods
  private Experiment createExperiment(String name, List<RuleAttributes> ruleAttributes) {
    return Experiment.builder()
        .experimentId(UUID.randomUUID())
        .name(name)
        .experimentKey(name)
        .projectKey("test-project")
        .status(ExperimentStatus.LIVE)
        .exposure(100)
        .ruleAttributes(ruleAttributes)
        .build();
  }

  private List<RuleAttributes> createRuleAttributes() {
    return List.of(
        RuleAttributes.builder()
            .name("default_rule")
            .conditions(List.of(createCondition("platform", "STRING", "=", "iOS")))
            .build());
  }

  private List<RuleAttributes> createRuleWithCondition(
      String operand, String dataType, String operator, String value) {
    return List.of(
        RuleAttributes.builder()
            .name("test_rule")
            .conditions(List.of(createCondition(operand, dataType, operator, value)))
            .build());
  }

  private RuleAttributes.Condition createCondition(
      String operand, String dataType, String operator, String value) {
    return RuleAttributes.Condition.builder()
        .operand(operand)
        .operandDataType(dataType)
        .operator(operator)
        .value(value)
        .build();
  }
}
