package com.ascend.testlab.allocation.strategy.variantselection;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StratifiedVariantSelectionStrategy Tests")
class StratifiedVariantSelectionStrategyTest {

  private StratifiedVariantSelectionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new StratifiedVariantSelectionStrategy();
  }

  @Test
  @DisplayName("Should select variant when user cohort matches")
  void testSelectVariantWhenCohortMatches() {
    // Arrange
    Map<String, List<String>> weights = new HashMap<>();
    weights.put("control", List.of("premium", "beta"));
    weights.put("treatment", List.of("free", "trial"));

    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(weights).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertEquals("control", selectedVariant);
  }

  @Test
  @DisplayName("Should return null when user cohort does not match any variant")
  void testSelectVariantWhenCohortDoesNotMatch() {
    // Arrange
    Map<String, List<String>> weights = new HashMap<>();
    weights.put("control", List.of("premium", "beta"));
    weights.put("treatment", List.of("free", "trial"));

    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(weights).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("enterprise");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should return null when user has no cohorts")
  void testSelectVariantWhenUserHasNoCohorts() {
    // Arrange
    Map<String, List<String>> weights = new HashMap<>();
    weights.put("control", List.of("premium", "beta"));

    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(weights).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    String userId = "user123";

    // Act
    String selectedVariant1 = strategy.selectVariant(experiment, userId, null);
    String selectedVariant2 = strategy.selectVariant(experiment, userId, Collections.emptyList());

    // Assert
    assertNull(selectedVariant1);
    assertNull(selectedVariant2);
  }

  @Test
  @DisplayName("Should return null when variant weights is not StratifiedVariantWeights")
  void testSelectVariantWithWrongWeightsType() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should return null when selected variant not found in variant map")
  void testSelectVariantNotInVariantMap() {
    // Arrange
    Map<String, List<String>> weights = new HashMap<>();
    weights.put("control", List.of("premium"));
    weights.put("treatment", List.of("free"));

    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(weights).build();

    // Only include control variant in map, not treatment
    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("free");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should return null when variant map is null")
  void testSelectVariantWhenVariantMapIsNull() {
    // Arrange
    Map<String, List<String>> weights = new HashMap<>();
    weights.put("control", List.of("premium"));

    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(weights).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(null)
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should select first matching variant when user has multiple matching cohorts")
  void testSelectVariantWithMultipleMatchingCohorts() {
    // Arrange
    Map<String, List<String>> weights = new HashMap<>();
    weights.put("control", List.of("premium", "beta"));
    weights.put("treatment", List.of("trial"));

    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(weights).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("premium", "beta");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNotNull(selectedVariant);
    assertEquals("control", selectedVariant);
  }

  @Test
  @DisplayName("Should handle canHandle correctly for STRATIFIED experiments")
  void testCanHandle() {
    // Arrange
    StratifiedVariantWeights stratifiedWeights =
        StratifiedVariantWeights.builder().weights(new HashMap<>()).build();

    CohortVariantWeights cohortWeights =
        CohortVariantWeights.builder().weights(new HashMap<>()).build();

    Experiment stratifiedExperiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(stratifiedWeights)
            .build();

    Experiment cohortExperiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(AssignmentDomain.COHORT)
            .variantWeights(cohortWeights)
            .build();

    // Act & Assert
    assertTrue(strategy.canHandle(stratifiedExperiment));
    assertFalse(strategy.canHandle(cohortExperiment));
    assertFalse(strategy.canHandle(null));
  }

  @Test
  @DisplayName("Should return false for canHandle when assignment domain is null")
  void testCanHandleWithNullAssignmentDomain() {
    // Arrange
    StratifiedVariantWeights stratifiedWeights =
        StratifiedVariantWeights.builder().weights(new HashMap<>()).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(null)
            .variantWeights(stratifiedWeights)
            .build();

    // Act & Assert
    assertFalse(strategy.canHandle(experiment));
  }

  @Test
  @DisplayName("Should return false for canHandle when variant weights is wrong type")
  void testCanHandleWithWrongWeightsType() {
    // Arrange
    CohortVariantWeights cohortWeights =
        CohortVariantWeights.builder().weights(new HashMap<>()).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(cohortWeights)
            .build();

    // Act & Assert
    assertFalse(strategy.canHandle(experiment));
  }
}
