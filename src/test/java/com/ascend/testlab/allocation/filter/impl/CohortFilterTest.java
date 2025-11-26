package com.ascend.testlab.allocation.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CohortFilter Tests")
class CohortFilterTest {

  @Test
  @DisplayName("Should return all experiments without cohort restrictions when user has no cohorts")
  void testFilterWithNoUserCohorts() {
    // Arrange
    CohortFilter filter = new CohortFilter(Collections.emptyList());

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", Collections.emptyList()),
            createExperiment("exp2", List.of("cohort1")),
            createExperiment("exp3", List.of("cohort2", "cohort3")));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals("exp1", result.get(0).getName());
  }

  @Test
  @DisplayName(
      "Should return all experiments without cohort restrictions when user cohorts is null")
  void testFilterWithNullUserCohorts() {
    // Arrange
    CohortFilter filter = new CohortFilter(null);

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", Collections.emptyList()),
            createExperiment("exp2", List.of("cohort1")),
            createExperiment("exp3", List.of("cohort2")));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals("exp1", result.get(0).getName());
  }

  @Test
  @DisplayName("Should return all experiments when no cohort restrictions exist")
  void testFilterWithNoCohortRestrictedExperiments() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("cohort1", "cohort2"));

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
  @DisplayName("Should filter experiments that match user cohorts")
  void testFilterExperimentsMatchingUserCohorts() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("premium", "beta"));

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", List.of("premium")),
            createExperiment("exp2", List.of("free")),
            createExperiment("exp3", List.of("beta")),
            createExperiment("exp4", List.of("alpha")),
            createExperiment("exp5", Collections.emptyList()));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(3, result.size());
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp1")));
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp3")));
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp5")));
  }

  @Test
  @DisplayName("Should handle experiments with multiple cohorts")
  void testFilterExperimentsWithMultipleCohorts() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("premium"));

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", List.of("premium", "beta")),
            createExperiment("exp2", List.of("free", "alpha")),
            createExperiment("exp3", List.of("premium", "free", "beta")));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp1")));
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp3")));
  }

  @Test
  @DisplayName("Should filter when user has multiple cohorts and experiment matches at least one")
  void testFilterWithMultipleUserCohortsPartialMatch() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("premium", "beta", "enterprise"));

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", List.of("premium")),
            createExperiment("exp2", List.of("free")),
            createExperiment("exp3", List.of("beta")),
            createExperiment("exp4", List.of("enterprise", "vip")));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(3, result.size());
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp1")));
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp3")));
    assertTrue(result.stream().anyMatch(e -> e.getName().equals("exp4")));
  }

  @Test
  @DisplayName("Should filter out all experiments when no cohorts match")
  void testFilterWhenNoCohortMatches() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("premium"));

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", List.of("free")),
            createExperiment("exp2", List.of("basic")),
            createExperiment("exp3", List.of("trial")));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should handle empty experiments list")
  void testFilterWithEmptyExperimentsList() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("premium"));
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
    CohortFilter filter1 = new CohortFilter(List.of("premium"));
    CohortFilter filter2 = new CohortFilter(List.of("beta"));

    filter1.setNext(filter2);

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", List.of("premium", "beta")),
            createExperiment("exp2", List.of("premium")),
            createExperiment("exp3", List.of("beta")));

    // Act
    List<Experiment> result = filter1.filter(experiments);

    // Assert
    // Only exp1 has both premium (first filter) and beta (second filter)
    assertEquals(1, result.size());
    assertEquals("exp1", result.get(0).getName());
  }

  @Test
  @DisplayName("Should be case-sensitive for cohort matching")
  void testCaseSensitiveCohortMatching() {
    // Arrange
    CohortFilter filter = new CohortFilter(List.of("Premium"));

    List<Experiment> experiments =
        List.of(
            createExperiment("exp1", List.of("premium")),
            createExperiment("exp2", List.of("Premium")));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals("exp2", result.get(0).getName());
  }

  private Experiment createExperiment(String name, List<String> cohorts) {
    return Experiment.builder()
        .experimentId(UUID.randomUUID())
        .name(name)
        .key(name)
        .projectKey("test-project")
        .status(ExperimentStatus.LIVE)
        .exposure(100)
        .cohorts(cohorts)
        .build();
  }
}
