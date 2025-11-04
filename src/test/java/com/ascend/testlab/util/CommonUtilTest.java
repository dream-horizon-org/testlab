package com.ascend.testlab.util;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
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
  @DisplayName("validateProjectKey Tests")
  class ValidateProjectKeyTests {

    @Test
    @DisplayName("Should throw RestException for null projectKey")
    void testValidateProjectKeyNull() {
      RestException ex =
          assertThrows(RestException.class, () -> CommonUtil.validateProjectKey(null));
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getErrorCode(), ex.getErrorCode());
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getErrorMessage(), ex.getErrorMessage());
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getHttpStatusCode(), ex.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should throw RestException for empty projectKey")
    void testValidateProjectKeyEmpty() {
      RestException ex = assertThrows(RestException.class, () -> CommonUtil.validateProjectKey(""));
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getErrorCode(), ex.getErrorCode());
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getErrorMessage(), ex.getErrorMessage());
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getHttpStatusCode(), ex.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should throw RestException for blank projectKey")
    void testValidateProjectKeyBlank() {
      RestException ex =
          assertThrows(RestException.class, () -> CommonUtil.validateProjectKey("   "));
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getErrorCode(), ex.getErrorCode());
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getErrorMessage(), ex.getErrorMessage());
      assertEquals(ErrorEnum.INVALID_PROJECT_KEY.getHttpStatusCode(), ex.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should pass for non-blank projectKey")
    void testValidateProjectKeyValid() {
      assertDoesNotThrow(() -> CommonUtil.validateProjectKey("test-project"));
    }
  }
}
