package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.NameAvailabilityDAO;
import com.ascend.testlab.dto.response.NameAvailabilityResponse;
import com.ascend.testlab.service.impl.NameAvailabilityServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for NameAvailabilityService.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("NameAvailabilityService Tests")
class NameAvailabilityServiceTest {

  @Mock private NameAvailabilityDAO nameAvailabilityDAO;
  private NameAvailabilityService nameAvailabilityService;
  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";
  private final String testExperimentName = "test-experiment";

  @BeforeEach
  void setUp() {
    nameAvailabilityService = new NameAvailabilityServiceImpl(nameAvailabilityDAO);
  }

  @Nested
  @DisplayName("Name Availability Success Cases")
  class NameAvailabilitySuccessTests {
    @Test
    @DisplayName("Should return available=true when experiment name does not exist")
    void testIsExperimentNameAvailable_Available() {
      // Arrange
      when(nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<NameAvailabilityResponse> testObserver =
          nameAvailabilityService
              .isExperimentNameAvailable(testProjectKey, testExperimentName)
              .test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      NameAvailabilityResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertTrue(response.isAvailable());
      assertNotNull(response.message());
      assertTrue(
          response.message().contains(testExperimentName)
              && response.message().contains(testProjectKey)
              && response.message().contains("available"));
      verify(nameAvailabilityDAO, times(1))
          .isExperimentNameAvailable(testProjectKey, testExperimentName);
    }

    @Test
    @DisplayName("Should return available=false when experiment name already exists")
    void testIsExperimentNameAvailable_NotAvailable() {
      // Arrange
      when(nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName))
          .thenReturn(Single.just(false));

      // Act
      TestObserver<NameAvailabilityResponse> testObserver =
          nameAvailabilityService
              .isExperimentNameAvailable(testProjectKey, testExperimentName)
              .test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      NameAvailabilityResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertFalse(response.isAvailable());
      assertNotNull(response.message());
      assertTrue(
          response.message().contains(testExperimentName)
              && response.message().contains(testProjectKey)
              && response.message().contains("already exists"));
      verify(nameAvailabilityDAO, times(1))
          .isExperimentNameAvailable(testProjectKey, testExperimentName);
    }
  }

  @Nested
  @DisplayName("Name Availability Error Handling")
  class NameAvailabilityErrorTests {
    @Test
    @DisplayName("Should throw RestException when name availability check fails in DAO")
    void testIsExperimentNameAvailable_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName))
          .thenReturn(Single.error(dbException));

      // Act
      Single<NameAvailabilityResponse> result =
          nameAvailabilityService.isExperimentNameAvailable(testProjectKey, testExperimentName);
      TestObserver<NameAvailabilityResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(nameAvailabilityDAO, times(1))
          .isExperimentNameAvailable(testProjectKey, testExperimentName);
    }
  }
}
