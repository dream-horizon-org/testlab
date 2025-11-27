package com.ascend.testlab.operation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RelationalOperation Tests")
class RelationalOperationTest {

  @Nested
  @DisplayName("Greater Than Tests")
  class GreaterThanTests {

    @Test
    @DisplayName("Should return true when first operand is greater")
    void testIsGreaterThan_True() {
      assertTrue(RelationalOperation.isGreaterThan(5, 3));
      assertTrue(RelationalOperation.isGreaterThan("b", "a"));
      assertTrue(RelationalOperation.isGreaterThan(10L, 5L));
    }

    @Test
    @DisplayName("Should return false when first operand is not greater")
    void testIsGreaterThan_False() {
      assertFalse(RelationalOperation.isGreaterThan(3, 5));
      assertFalse(RelationalOperation.isGreaterThan(5, 5));
      assertFalse(RelationalOperation.isGreaterThan("a", "b"));
    }
  }

  @Nested
  @DisplayName("Greater Than Or Equal Tests")
  class GreaterThanOrEqualTests {

    @Test
    @DisplayName("Should return true when first operand is greater or equal")
    void testIsGreaterThanOrEqual_True() {
      assertTrue(RelationalOperation.isGreaterThanOrEqual(5, 3));
      assertTrue(RelationalOperation.isGreaterThanOrEqual(5, 5));
      assertTrue(RelationalOperation.isGreaterThanOrEqual("b", "a"));
    }

    @Test
    @DisplayName("Should return false when first operand is less")
    void testIsGreaterThanOrEqual_False() {
      assertFalse(RelationalOperation.isGreaterThanOrEqual(3, 5));
      assertFalse(RelationalOperation.isGreaterThanOrEqual("a", "b"));
    }
  }

  @Nested
  @DisplayName("Less Than Tests")
  class LessThanTests {

    @Test
    @DisplayName("Should return true when first operand is less")
    void testIsLessThan_True() {
      assertTrue(RelationalOperation.isLessThan(3, 5));
      assertTrue(RelationalOperation.isLessThan("a", "b"));
      assertTrue(RelationalOperation.isLessThan(5L, 10L));
    }

    @Test
    @DisplayName("Should return false when first operand is not less")
    void testIsLessThan_False() {
      assertFalse(RelationalOperation.isLessThan(5, 3));
      assertFalse(RelationalOperation.isLessThan(5, 5));
      assertFalse(RelationalOperation.isLessThan("b", "a"));
    }
  }

  @Nested
  @DisplayName("Less Than Or Equal Tests")
  class LessThanOrEqualTests {

    @Test
    @DisplayName("Should return true when first operand is less or equal")
    void testIsLessThanOrEqual_True() {
      assertTrue(RelationalOperation.isLessThanOrEqual(3, 5));
      assertTrue(RelationalOperation.isLessThanOrEqual(5, 5));
      assertTrue(RelationalOperation.isLessThanOrEqual("a", "b"));
    }

    @Test
    @DisplayName("Should return false when first operand is greater")
    void testIsLessThanOrEqual_False() {
      assertFalse(RelationalOperation.isLessThanOrEqual(5, 3));
      assertFalse(RelationalOperation.isLessThanOrEqual("b", "a"));
    }
  }

  @Nested
  @DisplayName("Equality Tests")
  class EqualityTests {

    @Test
    @DisplayName("Should return true when operands are equal (Comparable)")
    void testIsEqual_Comparable_True() {
      assertTrue(RelationalOperation.isEqual(5, 5));
      assertTrue(RelationalOperation.isEqual("test", "test"));
      assertTrue(RelationalOperation.isEqual(10L, 10L));
    }

    @Test
    @DisplayName("Should return false when operands are not equal (Comparable)")
    void testIsEqual_Comparable_False() {
      assertFalse(RelationalOperation.isEqual(5, 3));
      assertFalse(RelationalOperation.isEqual("test", "other"));
    }

    @Test
    @DisplayName("Should return true when objects are equal")
    void testIsEqual_Object_True() {
      String str1 = "test";
      String str2 = "test";
      assertTrue(RelationalOperation.isEqual((Object) str1, (Object) str2));
    }

    @Test
    @DisplayName("Should return false when objects are not equal")
    void testIsEqual_Object_False() {
      assertFalse(RelationalOperation.isEqual((Object) "test", (Object) "other"));
    }
  }

  @Nested
  @DisplayName("Inequality Tests")
  class InequalityTests {

    @Test
    @DisplayName("Should return true when operands are not equal (Comparable)")
    void testIsNotEqual_Comparable_True() {
      assertTrue(RelationalOperation.isNotEqual(5, 3));
      assertTrue(RelationalOperation.isNotEqual("test", "other"));
    }

    @Test
    @DisplayName("Should return false when operands are equal (Comparable)")
    void testIsNotEqual_Comparable_False() {
      assertFalse(RelationalOperation.isNotEqual(5, 5));
      assertFalse(RelationalOperation.isNotEqual("test", "test"));
    }

    @Test
    @DisplayName("Should return true when objects are not equal")
    void testIsNotEqual_Object_True() {
      assertTrue(RelationalOperation.isNotEqual((Object) "test", (Object) "other"));
    }

    @Test
    @DisplayName("Should return false when objects are equal")
    void testIsNotEqual_Object_False() {
      String str1 = "test";
      String str2 = "test";
      assertFalse(RelationalOperation.isNotEqual((Object) str1, (Object) str2));
    }
  }

  @Nested
  @DisplayName("Contains Tests")
  class ContainsTests {

    @Test
    @DisplayName("Should return true when collection contains element")
    void testContains_CollectionElement_True() {
      List<String> collection = Arrays.asList("apple", "banana", "cherry");
      assertTrue(RelationalOperation.contains("banana", collection));
    }

    @Test
    @DisplayName("Should return false when collection does not contain element")
    void testContains_CollectionElement_False() {
      List<String> collection = Arrays.asList("apple", "banana", "cherry");
      assertFalse(RelationalOperation.contains("orange", collection));
    }

    @Test
    @DisplayName("Should return true when string contains substring")
    void testContains_String_True() {
      assertTrue(RelationalOperation.contains("test", "this is a test string"));
      assertTrue(RelationalOperation.contains("is", "this is a test"));
    }

    @Test
    @DisplayName("Should return false when string does not contain substring")
    void testContains_String_False() {
      assertFalse(RelationalOperation.contains("xyz", "this is a test string"));
    }

    @Test
    @DisplayName("Should return true when collection contains any element from another collection")
    void testContains_CollectionCollection_True() {
      List<String> collection1 = Arrays.asList("apple", "banana");
      List<String> collection2 = Arrays.asList("banana", "cherry", "date");
      assertTrue(RelationalOperation.contains(collection1, collection2));
    }

    @Test
    @DisplayName("Should return false when collections have no common elements")
    void testContains_CollectionCollection_False() {
      List<String> collection1 = Arrays.asList("apple", "banana");
      List<String> collection2 = Arrays.asList("cherry", "date");
      assertFalse(RelationalOperation.contains(collection1, collection2));
    }

    @Test
    @DisplayName("Should return true when regex pattern matches")
    void testContainsRegex_True() {
      assertTrue(RelationalOperation.containsRegex("test123", "\\d+"));
      assertTrue(RelationalOperation.containsRegex("hello@example.com", "@.*\\.com"));
    }

    @Test
    @DisplayName("Should return false when regex pattern does not match")
    void testContainsRegex_False() {
      assertFalse(RelationalOperation.containsRegex("teststring", "\\d+"));
    }
  }

  @Nested
  @DisplayName("Does Not Contain Tests")
  class DoesNotContainTests {

    @Test
    @DisplayName("Should return true when collection does not contain element")
    void testDoesNotContain_CollectionElement_True() {
      List<String> collection = Arrays.asList("apple", "banana", "cherry");
      assertTrue(RelationalOperation.doesNotContain("orange", collection));
    }

    @Test
    @DisplayName("Should return false when collection contains element")
    void testDoesNotContain_CollectionElement_False() {
      List<String> collection = Arrays.asList("apple", "banana", "cherry");
      assertFalse(RelationalOperation.doesNotContain("banana", collection));
    }

    @Test
    @DisplayName("Should return true when string does not contain substring")
    void testDoesNotContain_String_True() {
      assertTrue(RelationalOperation.doesNotContain("xyz", "this is a test string"));
    }

    @Test
    @DisplayName("Should return false when string contains substring")
    void testDoesNotContain_String_False() {
      assertFalse(RelationalOperation.doesNotContain("test", "this is a test string"));
    }

    @Test
    @DisplayName("Should return true when collections have no common elements")
    void testDoesNotContain_CollectionCollection_True() {
      List<String> collection1 = Arrays.asList("cherry", "date");
      List<String> collection2 = Arrays.asList("apple", "banana");
      assertTrue(RelationalOperation.doesNotContain(collection1, collection2));
    }

    @Test
    @DisplayName("Should return false when collections have common elements")
    void testDoesNotContain_CollectionCollection_False() {
      List<String> collection1 = Arrays.asList("banana", "cherry");
      List<String> collection2 = Arrays.asList("apple", "banana");
      assertFalse(RelationalOperation.doesNotContain(collection1, collection2));
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle empty collections")
    void testEmptyCollections() {
      List<String> emptyCollection = Collections.emptyList();
      List<String> nonEmptyCollection = Arrays.asList("apple", "banana");

      assertFalse(RelationalOperation.contains("test", emptyCollection));
      assertTrue(RelationalOperation.doesNotContain("test", emptyCollection));
      assertFalse(RelationalOperation.contains(emptyCollection, nonEmptyCollection));
      assertFalse(RelationalOperation.contains(nonEmptyCollection, emptyCollection));
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
      assertTrue(RelationalOperation.contains("", "test"));
      assertFalse(RelationalOperation.doesNotContain("", "test"));
      assertTrue(RelationalOperation.isEqual("", ""));
    }
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should throw exception when trying to instantiate")
    void testConstructorThrowsException() {
      assertThrows(
          java.lang.reflect.InvocationTargetException.class,
          () -> {
            java.lang.reflect.Constructor<RelationalOperation> constructor =
                RelationalOperation.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
          });
    }
  }
}
