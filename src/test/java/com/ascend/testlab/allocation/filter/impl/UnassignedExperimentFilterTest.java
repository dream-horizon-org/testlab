package com.ascend.testlab.allocation.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UnassignedExperimentFilter Tests")
class UnassignedExperimentFilterTest {

  @Test
  @DisplayName("Should return all experiments when user has no assignments")
  void testFilterWithNoUserAssignments() {
    // Arrange
    List<UserExperimentMap> userAssignments = Collections.emptyList();
    UnassignedExperimentFilter filter = new UnassignedExperimentFilter(userAssignments);

    UUID exp1Id = UUID.randomUUID();
    UUID exp2Id = UUID.randomUUID();
    List<Experiment> experiments =
        List.of(createExperiment(exp1Id, "exp1"), createExperiment(exp2Id, "exp2"));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(e -> e.getExperimentId().equals(exp1Id)));
    assertTrue(result.stream().anyMatch(e -> e.getExperimentId().equals(exp2Id)));
  }

  @Test
  @DisplayName("Should return all experiments when user assignments is null")
  void testFilterWithNullUserAssignments() {
    // Arrange
    UnassignedExperimentFilter filter = new UnassignedExperimentFilter(null);

    UUID exp1Id = UUID.randomUUID();
    UUID exp2Id = UUID.randomUUID();
    List<Experiment> experiments =
        List.of(createExperiment(exp1Id, "exp1"), createExperiment(exp2Id, "exp2"));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("Should filter out assigned experiments")
  void testFilterOutAssignedExperiments() {
    // Arrange
    UUID exp1Id = UUID.randomUUID();
    UUID exp2Id = UUID.randomUUID();
    UUID exp3Id = UUID.randomUUID();

    List<UserExperimentMap> userAssignments =
        List.of(
            UserExperimentMap.builder().experimentId(exp1Id).variantName("control").build(),
            UserExperimentMap.builder().experimentId(exp3Id).variantName("variant1").build());

    UnassignedExperimentFilter filter = new UnassignedExperimentFilter(userAssignments);

    List<Experiment> experiments =
        List.of(
            createExperiment(exp1Id, "exp1"),
            createExperiment(exp2Id, "exp2"),
            createExperiment(exp3Id, "exp3"));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals(exp2Id, result.get(0).getExperimentId());
  }

  @Test
  @DisplayName("Should return empty list when all experiments are assigned")
  void testFilterWhenAllExperimentsAssigned() {
    // Arrange
    UUID exp1Id = UUID.randomUUID();
    UUID exp2Id = UUID.randomUUID();

    List<UserExperimentMap> userAssignments =
        List.of(
            UserExperimentMap.builder().experimentId(exp1Id).variantName("control").build(),
            UserExperimentMap.builder().experimentId(exp2Id).variantName("variant1").build());

    UnassignedExperimentFilter filter = new UnassignedExperimentFilter(userAssignments);

    List<Experiment> experiments =
        List.of(createExperiment(exp1Id, "exp1"), createExperiment(exp2Id, "exp2"));

    // Act
    List<Experiment> result = filter.filter(experiments);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should handle empty experiments list")
  void testFilterWithEmptyExperimentsList() {
    // Arrange
    List<UserExperimentMap> userAssignments =
        List.of(
            UserExperimentMap.builder()
                .experimentId(UUID.randomUUID())
                .variantName("control")
                .build());

    UnassignedExperimentFilter filter = new UnassignedExperimentFilter(userAssignments);
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
    UUID exp1Id = UUID.randomUUID();
    UUID exp2Id = UUID.randomUUID();

    List<UserExperimentMap> userAssignments =
        List.of(UserExperimentMap.builder().experimentId(exp1Id).variantName("control").build());

    UnassignedExperimentFilter filter1 = new UnassignedExperimentFilter(userAssignments);
    UnassignedExperimentFilter filter2 = new UnassignedExperimentFilter(Collections.emptyList());

    filter1.setNext(filter2);

    List<Experiment> experiments =
        List.of(createExperiment(exp1Id, "exp1"), createExperiment(exp2Id, "exp2"));

    // Act
    List<Experiment> result = filter1.filter(experiments);

    // Assert
    assertEquals(1, result.size());
    assertEquals(exp2Id, result.get(0).getExperimentId());
  }

  private Experiment createExperiment(UUID id, String name) {
    return Experiment.builder()
        .experimentId(id)
        .name(name)
        .key(name)
        .projectKey("test-project")
        .status(ExperimentStatus.LIVE)
        .exposure(100)
        .build();
  }
}
