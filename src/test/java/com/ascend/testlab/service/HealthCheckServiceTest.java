package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.dto.response.HealthCheckResponse;
import com.ascend.testlab.service.impl.HealthCheckServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for HealthCheckServiceImpl.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("HealthCheckService Tests")
public class HealthCheckServiceTest {

  private HealthCheckService healthCheckService;

  @Mock private HealthCheckDAO healthCheckDAO;

  @BeforeEach
  void setUp() {
    healthCheckService = new HealthCheckServiceImpl(healthCheckDAO);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create service with valid DAO")
    void testConstructorWithValidDAO() {
      // Act
      HealthCheckServiceImpl service = new HealthCheckServiceImpl(healthCheckDAO);

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should create service with null DAO")
    void testConstructorWithNullDAO() {
      // Act - Constructor doesn't validate null, but will fail at runtime
      HealthCheckServiceImpl service = new HealthCheckServiceImpl(null);

      // Assert
      assertNotNull(service);
    }
  }

  @Nested
  @DisplayName("Health Check Success Tests")
  class HealthCheckSuccessTests {

    @Test
    @DisplayName("Should return healthy response when all services are up")
    void testHealthCheckAllServicesUp() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      HealthCheckResponse response = testObserver.values().get(0);
      assertTrue(response.isPgReaderUp());
      assertTrue(response.isAerospikeUp());
      assertFalse(response.isUnderMaintenance());
    }

    @Test
    @DisplayName("Should return response when only PostgreSQL is up")
    void testHealthCheckOnlyPostgreSQLUp() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(false));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      HealthCheckResponse response = testObserver.values().get(0);
      assertTrue(response.isPgReaderUp());
      assertFalse(response.isAerospikeUp());
      assertFalse(response.isUnderMaintenance());
    }

    @Test
    @DisplayName("Should return response when only Aerospike is up")
    void testHealthCheckOnlyAerospikeUp() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(false));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      HealthCheckResponse response = testObserver.values().get(0);
      assertFalse(response.isPgReaderUp());
      assertTrue(response.isAerospikeUp());
      assertFalse(response.isUnderMaintenance());
    }

    @Test
    @DisplayName("Should return response with maintenance mode true")
    void testHealthCheckUnderMaintenance() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(true));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      HealthCheckResponse response = testObserver.values().get(0);
      assertTrue(response.isPgReaderUp());
      assertTrue(response.isAerospikeUp());
      assertTrue(response.isUnderMaintenance());
    }
  }

  @Nested
  @DisplayName("Health Check Failure Tests")
  class HealthCheckFailureTests {

    @Test
    @DisplayName("Should throw exception when both services are down")
    void testHealthCheckBothServicesDown() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(false));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(false));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }

    @Test
    @DisplayName("Should throw exception when both services down and under maintenance")
    void testHealthCheckBothServicesDownUnderMaintenance() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(false));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(false));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(true));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
    }
  }

  @Nested
  @DisplayName("DAO Error Handling Tests")
  class DAOErrorHandlingTests {

    @Test
    @DisplayName("Should propagate error when PostgreSQL check fails")
    void testPostgreSQLCheckError() {
      // Arrange
      RuntimeException exception = new RuntimeException("PostgreSQL connection failed");
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.error(exception));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }

    @Test
    @DisplayName("Should propagate error when Aerospike check fails")
    void testAerospikeCheckError() {
      // Arrange
      RuntimeException exception = new RuntimeException("Aerospike connection failed");
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.error(exception));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }

    @Test
    @DisplayName("Should propagate error when maintenance check fails")
    void testMaintenanceCheckError() {
      // Arrange
      RuntimeException exception = new RuntimeException("Maintenance check failed");
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.error(exception));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }
  }

  @Nested
  @DisplayName("Reactive Behavior Tests")
  class ReactiveBehaviorTests {

    @Test
    @DisplayName("Should call all DAO methods exactly once")
    void testDAOMethodsCalledOnce() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Assert
      testObserver.assertComplete();
      verify(healthCheckDAO, times(1)).isPgReaderConnected();
      verify(healthCheckDAO, times(1)).isAerospikeConnected();
      verify(healthCheckDAO, times(1)).isUnderMaintenance();
    }

    @Test
    @DisplayName("Should handle multiple subscriptions correctly")
    void testMultipleSubscriptions() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act - Each subscription triggers the Single chain again
      TestObserver<HealthCheckResponse> testObserver1 = healthCheckService.healthCheck().test();
      TestObserver<HealthCheckResponse> testObserver2 = healthCheckService.healthCheck().test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();

      // Verify called exactly twice (once per subscription)
      verify(healthCheckDAO, times(2)).isPgReaderConnected();
      verify(healthCheckDAO, times(2)).isAerospikeConnected();
      verify(healthCheckDAO, times(2)).isUnderMaintenance();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle concurrent health check calls")
    void testConcurrentHealthChecks() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isAerospikeConnected()).thenReturn(Single.just(true));
      when(healthCheckDAO.isUnderMaintenance()).thenReturn(Single.just(false));

      // Act
      TestObserver<HealthCheckResponse> testObserver1 = healthCheckService.healthCheck().test();
      TestObserver<HealthCheckResponse> testObserver2 = healthCheckService.healthCheck().test();
      TestObserver<HealthCheckResponse> testObserver3 = healthCheckService.healthCheck().test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      testObserver3.assertComplete();
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle delayed DAO responses")
    void testDelayedDAOResponses() {
      // Arrange
      when(healthCheckDAO.isPgReaderConnected())
          .thenReturn(Single.just(true).delay(100, TimeUnit.MILLISECONDS));
      when(healthCheckDAO.isAerospikeConnected())
          .thenReturn(Single.just(true).delay(100, TimeUnit.MILLISECONDS));
      when(healthCheckDAO.isUnderMaintenance())
          .thenReturn(Single.just(false).delay(100, TimeUnit.MILLISECONDS));

      // Act
      TestObserver<HealthCheckResponse> testObserver = healthCheckService.healthCheck().test();

      // Wait for completion
      testObserver.awaitDone(1, TimeUnit.SECONDS);

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle service instantiation correctly")
    void testServiceInstantiation() {
      // Act
      HealthCheckServiceImpl newService = new HealthCheckServiceImpl(healthCheckDAO);

      // Assert
      assertNotNull(newService);
    }
  }
}
