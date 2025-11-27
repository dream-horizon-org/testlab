package com.ascend.testlab.allocation.strategy.variantselection;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CohortVariantSelectionStrategy Tests")
class CohortVariantSelectionStrategyTest {

  private CohortVariantSelectionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new CohortVariantSelectionStrategy();
  }

  @Test
  @DisplayName("Should select a variant using ROUND_ROBIN strategy")
  void testSelectVariantWithRoundRobin() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0, "free", 30.0)).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.ROUND_ROBIN)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNotNull(selectedVariant);
    assertTrue(variants.containsKey(selectedVariant));
  }

  @Test
  @DisplayName("Should select a variant using RANDOM strategy")
  void testSelectVariantWithRandom() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.RANDOM)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user456";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNotNull(selectedVariant);
    assertTrue(variants.containsKey(selectedVariant));
  }

  @Test
  @DisplayName("Should return null when variant weights is not CohortVariantWeights")
  void testSelectVariantWithWrongWeightsType() {
    // Arrange
    StratifiedVariantWeights variantWeights =
        StratifiedVariantWeights.builder().weights(Map.of("control", List.of("premium"))).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.RANDOM)
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
  @DisplayName("Should return null when variants map is empty")
  void testSelectVariantWithEmptyVariantsMap() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.RANDOM)
            .variantWeights(variantWeights)
            .variants(Collections.emptyMap())
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should return null when variants map is null")
  void testSelectVariantWithNullVariantsMap() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.RANDOM)
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
  @DisplayName("Should select from all available variants regardless of user cohorts")
  void testSelectVariantIgnoresUserCohorts() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.ROUND_ROBIN)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    // Different user cohorts shouldn't affect selection
    String userId = "user123";

    // Act
    String selectedVariant1 = strategy.selectVariant(experiment, userId, List.of("premium"));
    String selectedVariant2 = strategy.selectVariant(experiment, userId, List.of("free"));
    String selectedVariant3 = strategy.selectVariant(experiment, userId, Collections.emptyList());

    // Assert - All should select a variant
    assertNotNull(selectedVariant1);
    assertNotNull(selectedVariant2);
    assertNotNull(selectedVariant3);

    // All should be the same for same userId with ROUND_ROBIN
    assertEquals(selectedVariant1, selectedVariant2);
    assertEquals(selectedVariant2, selectedVariant3);
  }

  @Test
  @DisplayName("Should handle experiments with multiple variants")
  void testSelectVariantWithMultipleVariants() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment1", Variant.builder().displayName("Treatment 1").build());
    variants.put("treatment2", Variant.builder().displayName("Treatment 2").build());
    variants.put("treatment3", Variant.builder().displayName("Treatment 3").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.ROUND_ROBIN)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    List<String> userCohorts = List.of("premium");
    String userId = "user123";

    // Act
    String selectedVariant = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert
    assertNotNull(selectedVariant);
    assertTrue(variants.containsKey(selectedVariant));
  }

  @Test
  @DisplayName("Should handle canHandle correctly for COHORT experiments")
  void testCanHandle() {
    // Arrange
    CohortVariantWeights cohortWeights =
        CohortVariantWeights.builder().weights(new HashMap<>()).build();

    StratifiedVariantWeights stratifiedWeights =
        StratifiedVariantWeights.builder().weights(new HashMap<>()).build();

    Experiment cohortExperiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(AssignmentDomain.COHORT)
            .variantWeights(cohortWeights)
            .build();

    Experiment stratifiedExperiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(AssignmentDomain.STRATIFIED)
            .variantWeights(stratifiedWeights)
            .build();

    // Act & Assert
    assertTrue(strategy.canHandle(cohortExperiment));
    assertFalse(strategy.canHandle(stratifiedExperiment));
    assertFalse(strategy.canHandle(null));
  }

  @Test
  @DisplayName("Should return false for canHandle when assignment domain is null")
  void testCanHandleWithNullAssignmentDomain() {
    // Arrange
    CohortVariantWeights cohortWeights =
        CohortVariantWeights.builder().weights(new HashMap<>()).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(null)
            .variantWeights(cohortWeights)
            .build();

    // Act & Assert
    assertFalse(strategy.canHandle(experiment));
  }

  @Test
  @DisplayName("Should return false for canHandle when variant weights is wrong type")
  void testCanHandleWithWrongWeightsType() {
    // Arrange
    StratifiedVariantWeights stratifiedWeights =
        StratifiedVariantWeights.builder().weights(new HashMap<>()).build();

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .assignmentDomain(AssignmentDomain.COHORT)
            .variantWeights(stratifiedWeights)
            .build();

    // Act & Assert
    assertFalse(strategy.canHandle(experiment));
  }

  @Test
  @DisplayName("Should consistently select same variant for same user with ROUND_ROBIN")
  void testConsistentSelectionWithRoundRobin() {
    // Arrange
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("premium", 70.0)).build();

    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("treatment", Variant.builder().displayName("Treatment").build());

    Experiment experiment =
        Experiment.builder()
            .experimentId(UUID.randomUUID())
            .name("test-experiment")
            .assignmentDomain(AssignmentDomain.COHORT)
            .distributionStrategy(DistributionStrategy.ROUND_ROBIN)
            .variantWeights(variantWeights)
            .variants(variants)
            .build();

    String userId = "user123";
    List<String> userCohorts = List.of("premium");

    // Act - Call multiple times
    String selectedVariant1 = strategy.selectVariant(experiment, userId, userCohorts);
    String selectedVariant2 = strategy.selectVariant(experiment, userId, userCohorts);
    String selectedVariant3 = strategy.selectVariant(experiment, userId, userCohorts);

    // Assert - Same user should get same variant with ROUND_ROBIN
    assertEquals(selectedVariant1, selectedVariant2);
    assertEquals(selectedVariant2, selectedVariant3);
  }
}
