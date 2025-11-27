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

@DisplayName("DefaultStrategy Tests")
class DefaultStrategyTest {

  private DefaultStrategy strategy;
  private Experiment concludedExperiment;
  private UUID experimentId;

  @BeforeEach
  void setUp() {
    strategy = new DefaultStrategy();
    experimentId = UUID.randomUUID();
    concludedExperiment = new Experiment();
    concludedExperiment.setExperimentId(experimentId);
  }

  @Nested
  @DisplayName("shouldOverride Tests")
  class ShouldOverrideTests {

    @Test
    @DisplayName("Should always return true when user was assigned")
    void testShouldOverride_WhenUserWasAssigned_ReturnsTrue() {
      UserExperimentMap assignment = new UserExperimentMap();
      assignment.setExperimentId(experimentId);
      assignment.setVariantName("variant-a");

      List<UserExperimentMap> existingAssignments = List.of(assignment);

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should always return true when user was not assigned")
    void testShouldOverride_WhenUserWasNotAssigned_ReturnsTrue() {
      UserExperimentMap assignment = new UserExperimentMap();
      assignment.setExperimentId(UUID.randomUUID());
      assignment.setVariantName("variant-a");

      List<UserExperimentMap> existingAssignments = List.of(assignment);

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should always return true when existing assignments are empty")
    void testShouldOverride_WhenNoExistingAssignments_ReturnsTrue() {
      List<UserExperimentMap> existingAssignments = new ArrayList<>();

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should always return true regardless of experiment matches")
    void testShouldOverride_MultipleAssignments_ReturnsTrue() {
      UserExperimentMap assignment1 = new UserExperimentMap();
      assignment1.setExperimentId(UUID.randomUUID());

      UserExperimentMap assignment2 = new UserExperimentMap();
      assignment2.setExperimentId(UUID.randomUUID());

      List<UserExperimentMap> existingAssignments = Arrays.asList(assignment1, assignment2);

      boolean result = strategy.shouldOverride(concludedExperiment, existingAssignments);

      assertTrue(result);
    }
  }

  @Nested
  @DisplayName("getStrategyName Tests")
  class GetStrategyNameTests {

    @Test
    @DisplayName("Should return correct strategy name")
    void testGetStrategyName() {
      assertEquals("BOTH", strategy.getStrategyName());
    }
  }
}
