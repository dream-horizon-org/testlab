package com.ascend.testlab.allocation.strategy.concludedexperiment;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UnassignedOnlyStrategy Tests")
class UnassignedOnlyStrategyTest {

  private UnassignedOnlyStrategy strategy;
  private Experiment concludedExperiment;
  private UUID experimentId;

  @BeforeEach
  void setUp() {
    strategy = new UnassignedOnlyStrategy();
    experimentId = UUID.randomUUID();
    concludedExperiment = new Experiment();
    concludedExperiment.setExperimentId(experimentId);
  }

  @Nested
  @DisplayName("shouldOverride Tests")
  class ShouldOverrideTests {

    @Test
    @DisplayName("Should return false when user was assigned to the experiment")
    void testShouldOverride_WhenUserWasAssigned_ReturnsFalse() {
      UserExperimentMap assignment1 = new UserExperimentMap();
      assignment1.setExperimentId(experimentId);
      assignment1.setVariantName("variant-a");

      UserExperimentMap assignment2 = new UserExperimentMap();
      assignment2.setExperimentId(UUID.randomUUID());
      assignment2.setVariantName("variant-b");

      List<UserExperimentMap> existingAssignments = Arrays.asList(assignment1, assignment2);

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when user was not assigned to the experiment")
    void testShouldOverride_WhenUserWasNotAssigned_ReturnsTrue() {
      UserExperimentMap assignment1 = new UserExperimentMap();
      assignment1.setExperimentId(UUID.randomUUID());
      assignment1.setVariantName("variant-a");

      UserExperimentMap assignment2 = new UserExperimentMap();
      assignment2.setExperimentId(UUID.randomUUID());
      assignment2.setVariantName("variant-b");

      List<UserExperimentMap> existingAssignments = Arrays.asList(assignment1, assignment2);

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return true when existing assignments are empty")
    void testShouldOverride_WhenNoExistingAssignments_ReturnsTrue() {
      List<UserExperimentMap> existingAssignments = new ArrayList<>();

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when experiment ID matches exactly")
    void testShouldOverride_ExactExperimentIdMatch_ReturnsFalse() {
      UserExperimentMap assignment = new UserExperimentMap();
      assignment.setExperimentId(experimentId);

      List<UserExperimentMap> existingAssignments = List.of(assignment);

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertFalse(result);
    }
  }

  @Nested
  @DisplayName("getStrategyName Tests")
  class GetStrategyNameTests {

    @Test
    @DisplayName("Should return correct strategy name")
    void testGetStrategyName() {
      assertEquals("UNASSIGNED_ONLY", strategy.getStrategyName());
    }
  }
}
