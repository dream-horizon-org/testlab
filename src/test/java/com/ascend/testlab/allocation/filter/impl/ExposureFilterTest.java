package com.ascend.testlab.allocation.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@DisplayName("ExposureFilter Tests")
class ExposureFilterTest {

  @Test
  @DisplayName("Should filter out experiments with zero exposure")
  void testFilterExperimentsWithZeroExposure() {
    // Arrange
    ExposureFilter filter = new ExposureFilter();

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", 0), createExperiment("exp2", 0), createExperiment("exp3", 0));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should include experiments with 100% exposure")
  void testFilterExperimentsWithFullExposure() {
    // Arrange
    ExposureFilter filter = new ExposureFilter();

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", 100),
            createExperiment("exp2", 100),
            createExperiment("exp3", 100));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(3, result.size());
  }

  @RepeatedTest(10)
  @DisplayName("Should filter experiments based on exposure percentage (probabilistic)")
  void testFilterExperimentsWithVariedExposure() {
    // Arrange
    ExposureFilter filter = new ExposureFilter();

    // Create experiments with different exposure percentages
    // Running multiple times due to randomness
    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", 100),
            createExperiment("exp2", 50),
            createExperiment("exp3", 25),
            createExperiment("exp4", 10),
            createExperiment("exp5", 0));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    // exp1 (100%) should always be included
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp1")));

    // exp5 (0%) should never be included
    assertFalse(result.stream().anyMatch(e -> e.getName().equals("exp5")));

    // Result size should be between 1 (only exp1) and 4 (all except exp5)
    assertTrue(result.size() >= 1 && result.size() <= 4);
  }

  @Test
  @DisplayName("Should handle empty experiments list")
  void testFilterWithEmptyExperimentsList() {
    // Arrange
    ExposureFilter filter = new ExposureFilter();
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
    ExposureFilter filter1 = new ExposureFilter();
    ExposureFilter filter2 = new ExposureFilter();

    filter1.setNext(filter2);

    List<Experiment> experiments =
        List.of(createExperiment("exp1", 100), createExperiment("exp2", 100));

    // Act
    List<Experiment> result = filter1.filter(experiments);

    // Assert
    // Both filters should pass 100% exposure experiments
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("Should handle experiments with various exposure values")
  void testFilterWithVariousExposureValues() {
    // Arrange
    ExposureFilter filter = new ExposureFilter();

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", 1),
            createExperiment("exp2", 5),
            createExperiment("exp3", 50),
            createExperiment("exp4", 95),
            createExperiment("exp5", 99),
            createExperiment("exp6", 100));

    // Act - Run multiple times due to randomness
    int totalRuns = 100;
    int exp1Count = 0;
    int exp6Count = 0;

    for (int i = 0; i < totalRuns; i++) {
      List<Experiment> result = filter.filter(experiments);
      if (result.stream().anyMatch(e -> e.getName().equals("exp1"))) {
        exp1Count++;
      }
      if (result.stream().anyMatch(e -> e.getName().equals("exp6"))) {
        exp6Count++;
      }
    }

    // Assert
    // exp6 (100%) should be included in all runs
    assertEquals(totalRuns, exp6Count);

    // exp1 (1%) should be included in very few runs (allow some margin for randomness)
    assertTrue(
        exp1Count < 10, "Expected exp1 (1% exposure) to appear less than 10 times out of 100");
  }

  private Experiment createExperiment(String name, int exposure) {
    return Experiment.builder()
        .experimentId(UUID.randomUUID())
        .name(name)
        .experimentKey(name)
        .projectKey("test-project")
        .status(ExperimentStatus.LIVE)
        .exposure(exposure)
        .build();
  }
}
