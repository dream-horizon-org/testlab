package com.ascend.testlab.allocation.builder;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AssignmentBuilder Tests")
class AssignmentBuilderTest {

  private Experiment experiment;
  private UUID experimentId;
  private Map<String, Variant> variants;

  @BeforeEach
  void setUp() {
    experimentId = UUID.randomUUID();
    experiment = new Experiment();
    experiment.setExperimentId(experimentId);
    experiment.setName("Test Experiment");

    Variant controlVariant = new Variant();
    controlVariant.setDisplayName("Control Variant");

    Variant treatmentVariant = new Variant();
    treatmentVariant.setDisplayName("Treatment Variant");

    variants = new HashMap<>();
    variants.put("control", controlVariant);
    variants.put("treatment", treatmentVariant);

    experiment.setVariants(variants);
  }

  @Nested
  @DisplayName("buildAssignment Tests")
  class BuildAssignmentTests {

    @Test
    @DisplayName("Should build valid UserExperimentMap")
    void testBuildAssignment_Success() {
      UserExperimentMap assignment = AssignmentBuilder.buildAssignment(experiment, "control");

      assertNotNull(assignment);
      assertEquals(experimentId, assignment.getExperimentId());
      assertEquals("Test Experiment", assignment.getExperimentName());
      assertEquals("control", assignment.getVariantName());
      assertNotNull(assignment.getVariant());
      assertEquals("Control Variant", assignment.getVariant().getDisplayName());
      assertEquals("ASSIGNED", assignment.getStatus());
      assertNotNull(assignment.getAssignedAt());
      assertTrue(assignment.getAssignedAt() > 0);
    }

    @Test
    @DisplayName("Should throw exception when experiment is null")
    void testBuildAssignment_NullExperiment() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> AssignmentBuilder.buildAssignment(null, "control"));

      assertEquals("Experiment and variant cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when variant name is null")
    void testBuildAssignment_NullVariantName() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> AssignmentBuilder.buildAssignment(experiment, null));

      assertEquals("Experiment and variant cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when experiment variants are null")
    void testBuildAssignment_NullVariants() {
      experiment.setVariants(null);

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> AssignmentBuilder.buildAssignment(experiment, "control"));

      assertTrue(exception.getMessage().contains("Experiment variant map cannot be null or empty"));
      assertTrue(exception.getMessage().contains(experimentId.toString()));
    }

    @Test
    @DisplayName("Should throw exception when experiment variants are empty")
    void testBuildAssignment_EmptyVariants() {
      experiment.setVariants(new HashMap<>());

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> AssignmentBuilder.buildAssignment(experiment, "control"));

      assertTrue(exception.getMessage().contains("Experiment variant map cannot be null or empty"));
      assertTrue(exception.getMessage().contains(experimentId.toString()));
    }

    @Test
    @DisplayName("Should throw exception when variant not found in experiment")
    void testBuildAssignment_VariantNotFound() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> AssignmentBuilder.buildAssignment(experiment, "nonexistent"));

      assertTrue(exception.getMessage().contains("Variant 'nonexistent' not found"));
      assertTrue(exception.getMessage().contains(experimentId.toString()));
    }
  }
}
