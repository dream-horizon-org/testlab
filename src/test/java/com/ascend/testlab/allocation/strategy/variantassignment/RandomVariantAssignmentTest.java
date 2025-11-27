package com.ascend.testlab.allocation.strategy.variantassignment;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.dto.entity.experiment.Variant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@DisplayName("RandomVariantAssignment Tests")
class RandomVariantAssignmentTest {

  private RandomVariantAssignment strategy;

  @BeforeEach
  void setUp() {
    strategy = new RandomVariantAssignment();
  }

  @Test
  @DisplayName("Should return null when variants list is null")
  void testSelectVariantWithNullVariants() {
    // Arrange
    String userId = "user123";

    // Act
    Variant selectedVariant = strategy.selectVariant(null, userId);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should return null when variants list is empty")
  void testSelectVariantWithEmptyVariants() {
    // Arrange
    List<Variant> variants = Collections.emptyList();
    String userId = "user123";

    // Act
    Variant selectedVariant = strategy.selectVariant(variants, userId);

    // Assert
    assertNull(selectedVariant);
  }

  @Test
  @DisplayName("Should return the only variant when only one variant exists")
  void testSelectVariantWithSingleVariant() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    List<Variant> variants = List.of(variant1);
    String userId = "user123";

    // Act
    Variant selectedVariant = strategy.selectVariant(variants, userId);

    // Assert
    assertNotNull(selectedVariant);
    assertEquals(variant1, selectedVariant);
  }

  @RepeatedTest(20)
  @DisplayName("Should select a variant from the list (probabilistic)")
  void testSelectVariantWithMultipleVariants() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment1").build();
    Variant variant3 = Variant.builder().displayName("Treatment2").build();
    List<Variant> variants = List.of(variant1, variant2, variant3);
    String userId = "user123";

    // Act
    Variant selectedVariant = strategy.selectVariant(variants, userId);

    // Assert
    assertNotNull(selectedVariant);
    assertTrue(variants.contains(selectedVariant));
  }

  @Test
  @DisplayName("Should distribute variants randomly across multiple users")
  void testRandomDistributionAcrossUsers() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);

    Map<Variant, Integer> selectionCount = new HashMap<>();
    selectionCount.put(variant1, 0);
    selectionCount.put(variant2, 0);

    int totalRuns = 1000;

    // Act - Simulate 1000 random assignments
    for (int i = 0; i < totalRuns; i++) {
      String userId = "user" + i;
      Variant selected = strategy.selectVariant(variants, userId);
      selectionCount.put(selected, selectionCount.get(selected) + 1);
    }

    // Assert - Both variants should be selected at least some times
    // With random distribution, each variant should get roughly 500 selections
    // We allow a wide margin (200-800) to account for randomness
    assertTrue(
        selectionCount.get(variant1) > 200 && selectionCount.get(variant1) < 800,
        "Expected variant1 count to be between 200-800, got: " + selectionCount.get(variant1));
    assertTrue(
        selectionCount.get(variant2) > 200 && selectionCount.get(variant2) < 800,
        "Expected variant2 count to be between 200-800, got: " + selectionCount.get(variant2));
  }

  @Test
  @DisplayName("Should potentially return different variants for same user (non-deterministic)")
  void testNonDeterministicForSameUser() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);
    String userId = "user123";

    Set<Variant> selectedVariants = new HashSet<>();
    int maxAttempts = 100;

    // Act - Try multiple times to see if we get different variants
    for (int i = 0; i < maxAttempts; i++) {
      Variant selected = strategy.selectVariant(variants, userId);
      selectedVariants.add(selected);

      // If we've seen both variants, we can confirm non-deterministic behavior
      if (selectedVariants.size() == 2) {
        break;
      }
    }

    // Assert - With 100 attempts and random selection, we should see both variants
    // This proves it's non-deterministic (same user can get different variants)
    assertEquals(
        2,
        selectedVariants.size(),
        "Expected to see both variants across multiple calls for same user");
  }

  @Test
  @DisplayName("Should handle variants with complex objects")
  void testSelectVariantWithComplexVariants() {
    // Arrange
    Variant variant1 =
        Variant.builder().displayName("Control").variables(Collections.emptyList()).build();
    Variant variant2 =
        Variant.builder().displayName("Treatment").variables(Collections.emptyList()).build();
    List<Variant> variants = List.of(variant1, variant2);
    String userId = "user123";

    // Act
    Variant selectedVariant = strategy.selectVariant(variants, userId);

    // Assert
    assertNotNull(selectedVariant);
    assertTrue(variants.contains(selectedVariant));
  }

  @Test
  @DisplayName("Should work with large list of variants")
  void testSelectVariantWithManyVariants() {
    // Arrange
    List<Variant> variants = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      variants.add(Variant.builder().displayName("Variant" + i).build());
    }
    String userId = "user123";

    // Act
    Variant selectedVariant = strategy.selectVariant(variants, userId);

    // Assert
    assertNotNull(selectedVariant);
    assertTrue(variants.contains(selectedVariant));
  }

  @Test
  @DisplayName("Should distribute across many variants reasonably evenly")
  void testDistributionAcrossManyVariants() {
    // Arrange
    List<Variant> variants = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      variants.add(Variant.builder().displayName("Variant" + i).build());
    }

    Map<Variant, Integer> selectionCount = new HashMap<>();
    for (Variant v : variants) {
      selectionCount.put(v, 0);
    }

    int totalRuns = 1000;

    // Act
    for (int i = 0; i < totalRuns; i++) {
      Variant selected = strategy.selectVariant(variants, "user" + i);
      selectionCount.put(selected, selectionCount.get(selected) + 1);
    }

    // Assert - Each variant should be selected at least once
    for (Variant v : variants) {
      assertTrue(
          selectionCount.get(v) > 0,
          "Variant " + v.getDisplayName() + " should be selected at least once");
    }

    // Each variant should get roughly 100 selections (with wide margin for randomness)
    for (Variant v : variants) {
      int count = selectionCount.get(v);
      assertTrue(
          count >= 30 && count <= 200,
          "Expected variant count to be between 30-200, got: "
              + count
              + " for "
              + v.getDisplayName());
    }
  }
}
