package com.ascend.testlab.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for CommonUtil.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CommonUtil Tests")
public class CommonUtilTest {

  @Nested
  @DisplayName("Number of Cores Tests")
  class NumberOfCoresTests {

    @Test
    @DisplayName("Should return positive number of cores")
    void testGetNumberOfCoresPositive() {
      // Act
      int numberOfCores = CommonUtil.getNumberOfCores();

      // Assert
      assertTrue(numberOfCores > 0);
    }

    @Test
    @DisplayName("Should return consistent number of cores on multiple calls")
    void testGetNumberOfCoresConsistency() {
      // Act
      int firstCall = CommonUtil.getNumberOfCores();
      int secondCall = CommonUtil.getNumberOfCores();
      int thirdCall = CommonUtil.getNumberOfCores();

      // Assert
      assertEquals(firstCall, secondCall);
      assertEquals(secondCall, thirdCall);
      assertEquals(firstCall, thirdCall);
    }

    @Test
    @DisplayName("Should return reasonable number of cores")
    void testGetNumberOfCoresReasonable() {
      // Act
      int numberOfCores = CommonUtil.getNumberOfCores();

      // Assert - Most systems have between 1 and 256 cores
      assertTrue(numberOfCores >= 1 && numberOfCores <= 256);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle concurrent calls correctly")
    void testGetNumberOfCoresConcurrentCalls() throws InterruptedException {
      // Arrange
      int threadCount = 10;
      CountDownLatch latch = new CountDownLatch(threadCount);
      ConcurrentHashMap<Integer, Integer> results = new ConcurrentHashMap<>();

      // Act
      for (int i = 0; i < threadCount; i++) {
        final int index = i;
        new Thread(
                () -> {
                  results.put(index, CommonUtil.getNumberOfCores());
                  latch.countDown();
                })
            .start();
      }
      latch.await();

      // Assert - All threads should get the same value
      int firstValue = results.get(0);
      results.values().forEach(value -> assertEquals(firstValue, value));
    }

    @Test
    @DisplayName("Should return same value across different class loaders")
    void testGetNumberOfCoresAcrossContexts() {
      // Act
      int firstCall = CommonUtil.getNumberOfCores();

      // Simulate different execution contexts
      ExecutorService executor = Executors.newFixedThreadPool(1);
      try {
        Future<Integer> future = executor.submit(CommonUtil::getNumberOfCores);
        int secondCall = future.get();

        // Assert
        assertEquals(firstCall, secondCall);
      } catch (Exception e) {
        fail("Should not throw exception: " + e.getMessage());
      } finally {
        executor.shutdown();
      }
    }
  }

  @Nested
  @DisplayName("Format Values For IN Clause Tests")
  class FormatValuesForInClauseTests {

    @Test
    @DisplayName("Should format multiple string values correctly")
    void testFormatMultipleStringValues() {
      // Arrange
      List<String> values = Arrays.asList("value1", "value2", "value3");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'value1','value2','value3'", result);
    }

    @Test
    @DisplayName("Should format single value correctly")
    void testFormatSingleValue() {
      // Arrange
      List<String> values = Collections.singletonList("singleValue");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'singleValue'", result);
    }

    @Test
    @DisplayName("Should return empty string for empty list")
    void testFormatEmptyList() {
      // Arrange
      List<String> values = Collections.emptyList();

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("", result);
    }

    @Test
    @DisplayName("Should filter out empty strings")
    void testFormatFilterEmptyStrings() {
      // Arrange
      List<String> values = Arrays.asList("value1", "", "value2", "", "value3");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'value1','value2','value3'", result);
    }

    @Test
    @DisplayName("Should handle integer values")
    void testFormatIntegerValues() {
      // Arrange
      List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'1','2','3','4','5'", result);
    }

    @Test
    @DisplayName("Should handle mixed numeric types")
    void testFormatMixedNumericTypes() {
      // Arrange
      List<Number> values = Arrays.asList(1, 2L, 3.14, 4.0f);

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'1','2','3.14','4.0'", result);
    }

    @Test
    @DisplayName("Should handle values with special characters")
    void testFormatValuesWithSpecialCharacters() {
      // Arrange
      List<String> values = Arrays.asList("value-1", "value_2", "value.3");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'value-1','value_2','value.3'", result);
    }

    @Test
    @DisplayName("Should handle values with spaces")
    void testFormatValuesWithSpaces() {
      // Arrange
      List<String> values = Arrays.asList("value one", "value two");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'value one','value two'", result);
    }

    @Test
    @DisplayName("Should return empty string when all values are empty")
    void testFormatAllEmptyValues() {
      // Arrange
      List<String> values = Arrays.asList("", "", "");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle boolean values")
    void testFormatBooleanValues() {
      // Arrange
      List<Boolean> values = Arrays.asList(true, false, true);

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'true','false','true'", result);
    }

    @Test
    @DisplayName("Should handle values with apostrophes")
    void testFormatValuesWithApostrophes() {
      // Arrange
      List<String> values = Arrays.asList("O'Brien", "d'Artagnan");

      // Act
      String result = CommonUtil.formatValuesForInClause(values);

      // Assert
      assertEquals("'O'Brien','d'Artagnan'", result);
    }
  }

  @Nested
  @DisplayName("Separate Comma Separated String Tests")
  class SeparateCommaSeparatedStringTests {

    @Test
    @DisplayName("Should separate comma-separated values correctly")
    void testSeparateCommaSeparatedValues() {
      // Arrange
      String values = "value1,value2,value3";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("value1", "value2", "value3"), result);
    }

    @Test
    @DisplayName("Should handle single value without comma")
    void testSeparateSingleValue() {
      // Arrange
      String values = "singleValue";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(1, result.size());
      assertEquals(Collections.singletonList("singleValue"), result);
    }

    @Test
    @DisplayName("Should trim whitespace around values")
    void testSeparateWithWhitespace() {
      // Arrange
      String values = "value1 , value2 , value3";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("value1", "value2", "value3"), result);
    }

    @Test
    @DisplayName("Should handle leading and trailing spaces")
    void testSeparateWithLeadingTrailingSpaces() {
      // Arrange
      String values = "  value1  ,  value2  ,  value3  ";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("value1", "value2", "value3"), result);
    }

    @Test
    @DisplayName("Should handle empty string")
    void testSeparateEmptyString() {
      // Arrange
      String values = "";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(1, result.size());
      assertEquals(Collections.singletonList(""), result);
    }

    @Test
    @DisplayName("Should handle string with only commas")
    void testSeparateOnlyCommas() {
      // Arrange
      String values = ",,,";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert - split() drops trailing empty strings
      assertEquals(0, result.size());
      assertEquals(Collections.emptyList(), result);
    }

    @Test
    @DisplayName("Should handle leading comma")
    void testSeparateLeadingComma() {
      // Arrange
      String values = ",value1,value2";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("", "value1", "value2"), result);
    }

    @Test
    @DisplayName("Should handle trailing comma")
    void testSeparateTrailingComma() {
      // Arrange
      String values = "value1,value2,";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert - split() drops trailing empty strings
      assertEquals(2, result.size());
      assertEquals(Arrays.asList("value1", "value2"), result);
    }

    @Test
    @DisplayName("Should handle consecutive commas")
    void testSeparateConsecutiveCommas() {
      // Arrange
      String values = "value1,,value2";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("value1", "", "value2"), result);
    }

    @Test
    @DisplayName("Should handle values with special characters")
    void testSeparateValuesWithSpecialCharacters() {
      // Arrange
      String values = "value-1,value_2,value.3";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("value-1", "value_2", "value.3"), result);
    }

    @Test
    @DisplayName("Should handle numeric strings")
    void testSeparateNumericStrings() {
      // Arrange
      String values = "1,2,3,4,5";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(5, result.size());
      assertEquals(Arrays.asList("1", "2", "3", "4", "5"), result);
    }

    @Test
    @DisplayName("Should handle values with spaces in them")
    void testSeparateValuesWithSpaces() {
      // Arrange
      String values = "value one,value two,value three";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("value one", "value two", "value three"), result);
    }

    @Test
    @DisplayName("Should handle mixed alphanumeric values")
    void testSeparateMixedAlphanumericValues() {
      // Arrange
      String values = "abc123,def456,ghi789";

      // Act
      List<String> result = CommonUtil.separateCommaSeparatedString(values);

      // Assert
      assertEquals(3, result.size());
      assertEquals(Arrays.asList("abc123", "def456", "ghi789"), result);
    }
  }
}
