package com.ascend.testlab.operation;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.attributes.RelationalOperator;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TypeComparison Tests")
class TypeComparisonTest {

  @Nested
  @DisplayName("Boolean Comparison Tests")
  class BooleanComparisonTests {

    @Test
    @DisplayName("Should return true when booleans are equal")
    void testCompareBoolean_Equal_True() {
      assertTrue(TypeComparison.compareBoolean("true", "true", RelationalOperator.EQ, null));
      assertTrue(TypeComparison.compareBoolean("false", "false", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return false when booleans are not equal")
    void testCompareBoolean_Equal_False() {
      assertFalse(TypeComparison.compareBoolean("true", "false", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return true when booleans are not equal")
    void testCompareBoolean_NotEqual_True() {
      assertTrue(TypeComparison.compareBoolean("true", "false", RelationalOperator.NEQ, null));
    }

    @Test
    @DisplayName("Should return false when booleans are equal")
    void testCompareBoolean_NotEqual_False() {
      assertFalse(TypeComparison.compareBoolean("true", "true", RelationalOperator.NEQ, null));
    }

    @Test
    @DisplayName("Should return false when operand1 is null")
    void testCompareBoolean_NullOperand() {
      assertFalse(TypeComparison.compareBoolean(null, "true", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return false for unsupported operators")
    void testCompareBoolean_UnsupportedOperator() {
      assertFalse(TypeComparison.compareBoolean("true", "true", RelationalOperator.GT, null));
    }
  }

  @Nested
  @DisplayName("Double Comparison Tests")
  class DoubleComparisonTests {

    @Test
    @DisplayName("Should return true when first double is greater")
    void testCompareDouble_GreaterThan_True() {
      assertTrue(TypeComparison.compareDouble("5.5", "3.3", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return false when first double is not greater")
    void testCompareDouble_GreaterThan_False() {
      assertFalse(TypeComparison.compareDouble("3.3", "5.5", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return true when first double is greater than or equal")
    void testCompareDouble_GreaterThanOrEqual_True() {
      assertTrue(TypeComparison.compareDouble("5.5", "5.5", RelationalOperator.GTE, null));
      assertTrue(TypeComparison.compareDouble("6.0", "5.5", RelationalOperator.GTE, null));
    }

    @Test
    @DisplayName("Should return true when first double is less")
    void testCompareDouble_LessThan_True() {
      assertTrue(TypeComparison.compareDouble("3.3", "5.5", RelationalOperator.LT, null));
    }

    @Test
    @DisplayName("Should return true when first double is less than or equal")
    void testCompareDouble_LessThanOrEqual_True() {
      assertTrue(TypeComparison.compareDouble("5.5", "5.5", RelationalOperator.LTE, null));
      assertTrue(TypeComparison.compareDouble("3.3", "5.5", RelationalOperator.LTE, null));
    }

    @Test
    @DisplayName("Should return true when doubles are equal")
    void testCompareDouble_Equal_True() {
      assertTrue(TypeComparison.compareDouble("5.5", "5.5", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return true when doubles are not equal")
    void testCompareDouble_NotEqual_True() {
      assertTrue(TypeComparison.compareDouble("5.5", "3.3", RelationalOperator.NEQ, null));
    }

    @Test
    @DisplayName("Should return false when operand1 is null")
    void testCompareDouble_NullOperand() {
      assertFalse(TypeComparison.compareDouble(null, "5.5", RelationalOperator.EQ, null));
    }
  }

  @Nested
  @DisplayName("Number Comparison Tests")
  class NumberComparisonTests {

    @Test
    @DisplayName("Should return true when first number is greater")
    void testCompareNumber_GreaterThan_True() {
      assertTrue(TypeComparison.compareNumber("100", "50", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return false when first number is not greater")
    void testCompareNumber_GreaterThan_False() {
      assertFalse(TypeComparison.compareNumber("50", "100", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return true when numbers are equal")
    void testCompareNumber_Equal_True() {
      assertTrue(TypeComparison.compareNumber("100", "100", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return false when operand1 is null")
    void testCompareNumber_NullOperand() {
      assertFalse(TypeComparison.compareNumber(null, "100", RelationalOperator.EQ, null));
    }
  }

  @Nested
  @DisplayName("String Comparison Tests")
  class StringComparisonTests {

    @Test
    @DisplayName("Should return true when string contains substring")
    void testCompareString_Contains_True() {
      assertTrue(
          TypeComparison.compareString("world", "hello world", RelationalOperator.CONTAINS, null));
    }

    @Test
    @DisplayName("Should return false when string does not contain substring")
    void testCompareString_Contains_False() {
      assertFalse(
          TypeComparison.compareString("xyz", "hello world", RelationalOperator.CONTAINS, null));
    }

    @Test
    @DisplayName("Should return true when string does not contain substring")
    void testCompareString_NotContains_True() {
      assertTrue(
          TypeComparison.compareString(
              "test", "hello world", RelationalOperator.NOT_CONTAINS, null));
    }

    {
      assertTrue(
          TypeComparison.compareString(
              "test", "hello world", RelationalOperator.NOT_CONTAINS, null));
    }

    @Test
    @DisplayName("Should return true when regex matches")
    void testCompareString_ContainsRegex_True() {
      assertTrue(
          TypeComparison.compareString("test123", "\\d+", RelationalOperator.CONTAINS_REGEX, null));
    }

    @Test
    @DisplayName("Should return false when regex does not match")
    void testCompareString_ContainsRegex_False() {
      assertFalse(
          TypeComparison.compareString(
              "teststring", "\\d+", RelationalOperator.CONTAINS_REGEX, null));
    }

    @Test
    @DisplayName("Should return true when strings are equal")
    void testCompareString_Equal_True() {
      assertTrue(TypeComparison.compareString("test", "test", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return true when first string is greater")
    void testCompareString_GreaterThan_True() {
      assertTrue(TypeComparison.compareString("b", "a", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return false when operand1 is null")
    void testCompareString_NullOperand() {
      assertFalse(TypeComparison.compareString(null, "test", RelationalOperator.EQ, null));
    }
  }

  @Nested
  @DisplayName("List Comparison Tests")
  class ListComparisonTests {

    @Test
    @DisplayName("Should return true when lists are equal (JSON)")
    void testCompareList_Equal_True() {
      String list1 = "[\"apple\", \"banana\"]";
      String list2 = "[\"apple\", \"banana\"]";
      assertTrue(TypeComparison.compareList(list1, list2, RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return false when lists are not equal (JSON)")
    void testCompareList_Equal_False() {
      String list1 = "[\"apple\", \"banana\"]";
      String list2 = "[\"cherry\", \"date\"]";
      assertFalse(TypeComparison.compareList(list1, list2, RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return true when lists are not equal (JSON)")
    void testCompareList_NotEqual_True() {
      String list1 = "[\"apple\", \"banana\"]";
      String list2 = "[\"cherry\", \"date\"]";
      assertTrue(TypeComparison.compareList(list1, list2, RelationalOperator.NEQ, null));
    }

    @Test
    @DisplayName("Should return true when list contains element (JSON)")
    void testCompareList_Contains_True() {
      String element = "banana";
      String list = "[\"apple\", \"banana\", \"cherry\"]";
      assertTrue(TypeComparison.compareList(element, list, RelationalOperator.CONTAINS, null));
    }

    @Test
    @DisplayName("Should return false when list does not contain element (JSON)")
    void testCompareList_Contains_False() {
      String element = "orange";
      String list = "[\"apple\", \"banana\", \"cherry\"]";
      assertFalse(TypeComparison.compareList(element, list, RelationalOperator.CONTAINS, null));
    }

    @Test
    @DisplayName("Should return true when list does not contain element (JSON)")
    void testCompareList_NotContains_True() {
      String element = "orange";
      String list = "[\"apple\", \"banana\", \"cherry\"]";
      assertTrue(TypeComparison.compareList(element, list, RelationalOperator.NOT_CONTAINS, null));
    }

    @Test
    @DisplayName("Should return true when list contains any element from another list")
    void testCompareList_ObjectLists_Contains_True() {
      List<String> list1 = Arrays.asList("apple", "banana");
      List<String> list2 = Arrays.asList("banana", "cherry");
      assertTrue(TypeComparison.compareList(list1, list2, RelationalOperator.CONTAINS));
    }

    @Test
    @DisplayName("Should return false when lists have no common elements")
    void testCompareList_ObjectLists_Contains_False() {
      List<String> list1 = Arrays.asList("apple", "banana");
      List<String> list2 = Arrays.asList("cherry", "date");
      assertFalse(TypeComparison.compareList(list1, list2, RelationalOperator.CONTAINS));
    }

    @Test
    @DisplayName("Should return true when list does not contain elements from another list")
    void testCompareList_ObjectLists_NotContains_True() {
      List<String> list1 = Arrays.asList("cherry", "date");
      List<String> list2 = Arrays.asList("apple", "banana");
      assertTrue(TypeComparison.compareList(list1, list2, RelationalOperator.NOT_CONTAINS));
    }

    @Test
    @DisplayName("Should return false for unsupported operators (JSON)")
    void testCompareList_UnsupportedOperator() {
      String list1 = "[\"apple\"]";
      String list2 = "[\"banana\"]";
      assertFalse(TypeComparison.compareList(list1, list2, RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return false for unsupported operators (Object lists)")
    void testCompareList_ObjectLists_UnsupportedOperator() {
      List<String> list1 = List.of("apple");
      List<String> list2 = List.of("banana");
      assertFalse(TypeComparison.compareList(list1, list2, RelationalOperator.EQ));
    }
  }

  @Nested
  @DisplayName("Object Comparison Tests")
  class ObjectComparisonTests {

    @Test
    @DisplayName("Should return true when JSON objects are equal")
    void testCompareObject_Equal_True() {
      String obj1 = "{\"name\":\"John\",\"age\":30}";
      String obj2 = "{\"name\":\"John\",\"age\":30}";
      assertTrue(TypeComparison.compareObject(obj1, obj2, RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return false when JSON objects are not equal")
    void testCompareObject_Equal_False() {
      String obj1 = "{\"name\":\"John\",\"age\":30}";
      String obj2 = "{\"name\":\"Jane\",\"age\":25}";
      assertFalse(TypeComparison.compareObject(obj1, obj2, RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return true when JSON objects are not equal")
    void testCompareObject_NotEqual_True() {
      String obj1 = "{\"name\":\"John\",\"age\":30}";
      String obj2 = "{\"name\":\"Jane\",\"age\":25}";
      assertTrue(TypeComparison.compareObject(obj1, obj2, RelationalOperator.NEQ, null));
    }

    @Test
    @DisplayName("Should return false when operand1 is null")
    void testCompareObject_NullOperand() {
      assertFalse(
          TypeComparison.compareObject(null, "{\"name\":\"John\"}", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return false for unsupported operators")
    void testCompareObject_UnsupportedOperator() {
      String obj1 = "{\"name\":\"John\"}";
      String obj2 = "{\"name\":\"Jane\"}";
      assertFalse(TypeComparison.compareObject(obj1, obj2, RelationalOperator.GT, null));
    }
  }

  @Nested
  @DisplayName("SemVer Comparison Tests")
  class SemVerComparisonTests {

    @Test
    @DisplayName("Should return true when first version is greater")
    void testCompareSemVer_GreaterThan_True() {
      assertTrue(TypeComparison.compareSemVer("2.0.0", "1.0.0", RelationalOperator.GT, null));
      assertTrue(TypeComparison.compareSemVer("1.2.0", "1.1.0", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return false when first version is not greater")
    void testCompareSemVer_GreaterThan_False() {
      assertFalse(TypeComparison.compareSemVer("1.0.0", "2.0.0", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return true when first version is greater than or equal")
    void testCompareSemVer_GreaterThanOrEqual_True() {
      assertTrue(TypeComparison.compareSemVer("1.0.0", "1.0.0", RelationalOperator.GTE, null));
      assertTrue(TypeComparison.compareSemVer("2.0.0", "1.0.0", RelationalOperator.GTE, null));
    }

    @Test
    @DisplayName("Should return true when first version is less")
    void testCompareSemVer_LessThan_True() {
      assertTrue(TypeComparison.compareSemVer("1.0.0", "2.0.0", RelationalOperator.LT, null));
    }

    @Test
    @DisplayName("Should return true when first version is less than or equal")
    void testCompareSemVer_LessThanOrEqual_True() {
      assertTrue(TypeComparison.compareSemVer("1.0.0", "1.0.0", RelationalOperator.LTE, null));
      assertTrue(TypeComparison.compareSemVer("1.0.0", "2.0.0", RelationalOperator.LTE, null));
    }

    @Test
    @DisplayName("Should return true when versions are equal")
    void testCompareSemVer_Equal_True() {
      assertTrue(TypeComparison.compareSemVer("1.0.0", "1.0.0", RelationalOperator.EQ, null));
    }

    @Test
    @DisplayName("Should return true when versions are not equal")
    void testCompareSemVer_NotEqual_True() {
      assertTrue(TypeComparison.compareSemVer("1.0.0", "2.0.0", RelationalOperator.NEQ, null));
    }

    @Test
    @DisplayName("Should handle version coercion")
    void testCompareSemVer_Coercion() {
      assertTrue(TypeComparison.compareSemVer("1.0", "0.9", RelationalOperator.GT, null));
      assertTrue(TypeComparison.compareSemVer("2", "1", RelationalOperator.GT, null));
    }

    @Test
    @DisplayName("Should return false when operand1 cannot be coerced")
    void testCompareSemVer_InvalidOperand() {
      assertFalse(TypeComparison.compareSemVer("invalid", "1.0.0", RelationalOperator.EQ, null));
    }
  }

  @Nested
  @DisplayName("Comparable Generic Tests")
  class ComparableGenericTests {

    @Test
    @DisplayName("Should correctly compare integers")
    void testCompareComparable_Integers() {
      assertTrue(TypeComparison.compareComparable(10, 5, RelationalOperator.GT, "test"));
      assertTrue(TypeComparison.compareComparable(10, 10, RelationalOperator.GTE, "test"));
      assertTrue(TypeComparison.compareComparable(5, 10, RelationalOperator.LT, "test"));
      assertTrue(TypeComparison.compareComparable(10, 10, RelationalOperator.LTE, "test"));
      assertTrue(TypeComparison.compareComparable(10, 10, RelationalOperator.EQ, "test"));
      assertTrue(TypeComparison.compareComparable(10, 5, RelationalOperator.NEQ, "test"));
    }

    @Test
    @DisplayName("Should correctly compare strings")
    void testCompareComparable_Strings() {
      assertTrue(TypeComparison.compareComparable("b", "a", RelationalOperator.GT, "test"));
      assertTrue(TypeComparison.compareComparable("a", "a", RelationalOperator.EQ, "test"));
      assertTrue(TypeComparison.compareComparable("a", "b", RelationalOperator.LT, "test"));
    }

    @Test
    @DisplayName("Should return false for unsupported operators")
    void testCompareComparable_UnsupportedOperator() {
      assertFalse(TypeComparison.compareComparable(10, 5, RelationalOperator.CONTAINS, "test"));
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
            java.lang.reflect.Constructor<TypeComparison> constructor =
                TypeComparison.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
          });
    }
  }
}
