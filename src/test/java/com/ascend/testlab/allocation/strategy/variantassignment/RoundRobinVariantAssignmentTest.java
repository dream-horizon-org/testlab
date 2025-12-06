package com.ascend.testlab.allocation.strategy.variantassignment;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.dto.entity.experiment.Variant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RoundRobinVariantAssignment Tests")
class RoundRobinVariantAssignmentTest {

  private RoundRobinVariantAssignment strategy;

  @BeforeEach
  void setUp() {
    strategy = new RoundRobinVariantAssignment();
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

  @Test
  @DisplayName("Should select a variant from the list")
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
  @DisplayName("Should consistently return same variant for same user (deterministic)")
  void testDeterministicForSameUser() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);
    String userId = "user123";

    // Act - Call multiple times for same user
    Variant selectedVariant1 = strategy.selectVariant(variants, userId);
    Variant selectedVariant2 = strategy.selectVariant(variants, userId);
    Variant selectedVariant3 = strategy.selectVariant(variants, userId);

    // Assert - Same user should always get same variant
    assertEquals(selectedVariant1, selectedVariant2);
    assertEquals(selectedVariant2, selectedVariant3);
  }

  @Test
  @DisplayName("Should distribute variants across different users")
  void testDistributionAcrossDifferentUsers() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);

    Map<Variant, Integer> selectionCount = new HashMap<>();
    selectionCount.put(variant1, 0);
    selectionCount.put(variant2, 0);

    int totalUsers = 1000;

    // Act - Assign variants to different users
    for (int i = 0; i < totalUsers; i++) {
      String userId = "user" + i;
      Variant selected = strategy.selectVariant(variants, userId);
      selectionCount.put(selected, selectionCount.get(selected) + 1);
    }

    // Assert - Distribution should be roughly even (both variants should be used)
    assertTrue(selectionCount.get(variant1) > 0, "Control variant should be selected");
    assertTrue(selectionCount.get(variant2) > 0, "Treatment variant should be selected");

    // With hash-based round-robin, expect roughly 50-50 distribution (allow 40-60% margin)
    int v1Count = selectionCount.get(variant1);
    assertTrue(
        v1Count >= 400 && v1Count <= 600,
        "Expected variant1 count to be between 400-600, got: " + v1Count);
  }

  @Test
  @DisplayName("Should distribute across three variants evenly")
  void testDistributionWithThreeVariants() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment1").build();
    Variant variant3 = Variant.builder().displayName("Treatment2").build();
    List<Variant> variants = List.of(variant1, variant2, variant3);

    Map<Variant, Integer> selectionCount = new HashMap<>();
    selectionCount.put(variant1, 0);
    selectionCount.put(variant2, 0);
    selectionCount.put(variant3, 0);

    int totalUsers = 1500;

    // Act
    for (int i = 0; i < totalUsers; i++) {
      String userId = "user" + i;
      Variant selected = strategy.selectVariant(variants, userId);
      selectionCount.put(selected, selectionCount.get(selected) + 1);
    }

    // Assert - Each variant should get roughly 1/3 of users (expect roughly 500 each)
    for (Variant v : variants) {
      int count = selectionCount.get(v);
      assertTrue(
          count >= 300 && count <= 700,
          "Expected count between 300-700, got: " + count + " for " + v.getDisplayName());
    }
  }

  @Test
  @DisplayName("Should handle different user IDs consistently")
  void testConsistencyWithDifferentUserIds() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);

    // Act & Assert - Test multiple different users
    String user1 = "alice@example.com";
    Variant alice1 = strategy.selectVariant(variants, user1);
    Variant alice2 = strategy.selectVariant(variants, user1);
    assertEquals(alice1, alice2, "Same user should get same variant");

    String user2 = "bob@example.com";
    Variant bob1 = strategy.selectVariant(variants, user2);
    Variant bob2 = strategy.selectVariant(variants, user2);
    assertEquals(bob1, bob2, "Same user should get same variant");

    String user3 = "charlie@example.com";
    Variant charlie1 = strategy.selectVariant(variants, user3);
    Variant charlie2 = strategy.selectVariant(variants, user3);
    assertEquals(charlie1, charlie2, "Same user should get same variant");
  }

  @Test
  @DisplayName("Should handle negative hash codes correctly")
  void testHandleNegativeHashCodes() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);

    // Find a user ID that produces a negative hash code
    String userIdWithNegativeHash = null;
    for (int i = 0; i < 10000; i++) {
      String testUserId = "user" + i;
      if (testUserId.hashCode() < 0) {
        userIdWithNegativeHash = testUserId;
        break;
      }
    }

    // If we found one, test it
    if (userIdWithNegativeHash != null) {
      String userId = userIdWithNegativeHash;

      // Act
      Variant selectedVariant = strategy.selectVariant(variants, userId);

      // Assert - Should not throw exception and should return a valid variant
      assertNotNull(selectedVariant);
      assertTrue(variants.contains(selectedVariant));

      // Should be deterministic
      Variant selectedVariant2 = strategy.selectVariant(variants, userId);
      assertEquals(selectedVariant, selectedVariant2);
    }
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

    // Should be deterministic
    Variant selectedVariant2 = strategy.selectVariant(variants, userId);
    assertEquals(selectedVariant, selectedVariant2);
  }

  @Test
  @DisplayName("Should distribute across many variants")
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

    int totalUsers = 1000;

    // Act
    for (int i = 0; i < totalUsers; i++) {
      Variant selected = strategy.selectVariant(variants, "user" + i);
      selectionCount.put(selected, selectionCount.get(selected) + 1);
    }

    // Assert - Each variant should be selected at least once
    for (Variant v : variants) {
      assertTrue(
          selectionCount.get(v) > 0,
          "Variant " + v.getDisplayName() + " should be selected at least once");
    }

    // Distribution should be relatively even (each should get roughly 100 selections)
    for (Variant v : variants) {
      int count = selectionCount.get(v);
      assertTrue(
          count >= 50 && count <= 200,
          "Expected count between 50-200, got: " + count + " for " + v.getDisplayName());
    }
  }

  @Test
  @DisplayName("Should handle UUID-like user IDs")
  void testWithUuidUserIds() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);

    String userId = "550e8400-e29b-41d4-a716-446655440000";

    // Act
    Variant selected1 = strategy.selectVariant(variants, userId);
    Variant selected2 = strategy.selectVariant(variants, userId);

    // Assert
    assertNotNull(selected1);
    assertEquals(selected1, selected2);
  }

  @Test
  @DisplayName("Should handle edge case with Integer.MIN_VALUE hash")
  void testHandleMinValueHash() {
    // Arrange
    Variant variant1 = Variant.builder().displayName("Control").build();
    Variant variant2 = Variant.builder().displayName("Treatment").build();
    List<Variant> variants = List.of(variant1, variant2);

    // Find a user ID that produces Integer.MIN_VALUE hash
    // Math.abs(Integer.MIN_VALUE) = Integer.MIN_VALUE (still negative)
    // This tests that the modulo operation handles this edge case correctly
    String userIdWithMinValue = null;

    // Search for a string that produces Integer.MIN_VALUE hash
    // "polygenelubricants" is a known string that produces Integer.MIN_VALUE
    if ("polygenelubricants".hashCode() == Integer.MIN_VALUE) {
      userIdWithMinValue = "polygenelubricants";
    }

    // If we didn't find one, search through common patterns
    if (userIdWithMinValue == null) {
      for (int i = 0; i < 100000; i++) {
        String testId = "user_" + i;
        if (testId.hashCode() == Integer.MIN_VALUE) {
          userIdWithMinValue = testId;
          break;
        }
      }
    }

    if (userIdWithMinValue != null) {
      // Act
      Variant selected = strategy.selectVariant(variants, userIdWithMinValue);

      // Assert - Should not throw exception and should return valid variant
      assertNotNull(selected, "Should handle Integer.MIN_VALUE hash without throwing exception");
      assertTrue(variants.contains(selected), "Selected variant should be in the list");

      // Verify consistency - same user should get same variant even with MIN_VALUE hash
      Variant selected2 = strategy.selectVariant(variants, userIdWithMinValue);
      assertEquals(
          selected, selected2, "Same user should get same variant even with MIN_VALUE hash");
    } else {
      // If we can't find Integer.MIN_VALUE, test with very large negative hash
      // by finding a string with negative hash close to MIN_VALUE
      String negativeHashUser = null;
      int closestToMin = 0;

      for (int i = 0; i < 10000; i++) {
        String testId = "negtest_" + i;
        int hash = testId.hashCode();
        if (hash < closestToMin) {
          closestToMin = hash;
          negativeHashUser = testId;
        }
      }

      if (negativeHashUser != null) {
        Variant selected = strategy.selectVariant(variants, negativeHashUser);
        assertNotNull(selected, "Should handle large negative hash");
        assertTrue(variants.contains(selected));
      }
    }
  }
}
