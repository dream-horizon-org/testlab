package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.dao.impl.HealthCheckDAOImpl;
import com.ascend.testlab.util.MaintenanceUtil;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for HealthCheckDAO.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("HealthCheckDAO Tests")
public class HealthCheckDAOTest {

  @Mock private AerospikeClient aerospikeClient;

  @Mock private MySQLReaderClient mySQLReaderClient;

  private HealthCheckDAO healthCheckDAO;
  private Vertx vertx;

  @BeforeEach
  void setUp(Vertx vertx) {
    this.vertx = vertx;
    this.healthCheckDAO = new HealthCheckDAOImpl(aerospikeClient, mySQLReaderClient);
  }

  @AfterEach
  void tearDown() {
    // Clear maintenance mode after each test
    MaintenanceUtil.clearMaintenance(vertx);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create DAO with valid dependencies")
    void testConstructorWithValidDependencies(VertxTestContext testContext) {
      // Act
      HealthCheckDAO dao = new HealthCheckDAOImpl(aerospikeClient, mySQLReaderClient);

      // Assert
      assertNotNull(dao);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("MySQL Reader Connection Tests")
  class MySQLReaderConnectionTests {

    @Test
    @DisplayName("Should return true when MySQL reader is connected")
    void testMySQLReaderConnected(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      verify(mySQLReaderClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when MySQL reader is not connected")
    void testMySQLReaderNotConnected(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(false));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(mySQLReaderClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when MySQL reader connection check throws error")
    void testMySQLReaderConnectionError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("MySQL connection failed");
      when(mySQLReaderClient.isConnected()).thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(mySQLReaderClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle timeout error from MySQL reader")
    void testMySQLReaderTimeout(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("Connection timeout")));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(mySQLReaderClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle null pointer exception from MySQL reader")
    void testMySQLReaderNullPointerException(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected())
          .thenReturn(Single.error(new NullPointerException("Null client")));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(mySQLReaderClient, times(1)).isConnected();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Aerospike Connection Tests")
  class AerospikeConnectionTests {

    @Test
    @DisplayName("Should return true when Aerospike is connected")
    void testAerospikeConnected(VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.isConnected()).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when Aerospike is not connected")
    void testAerospikeNotConnected(VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.isConnected()).thenReturn(Single.just(false));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when Aerospike connection check throws error")
    void testAerospikeConnectionError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Aerospike connection failed");
      when(aerospikeClient.isConnected()).thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle timeout error from Aerospike")
    void testAerospikeTimeout(VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("Connection timeout")));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle illegal state exception from Aerospike")
    void testAerospikeIllegalStateException(VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.isConnected())
          .thenReturn(Single.error(new IllegalStateException("Invalid state")));

      // Act
      TestObserver<Boolean> testObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Maintenance Mode Tests")
  class MaintenanceModeTests {

    @Test
    @DisplayName("Should return false when not under maintenance")
    void testNotUnderMaintenance(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      MaintenanceUtil.clearMaintenance(vertx);

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<Boolean> testObserver = healthCheckDAO.isUnderMaintenance().test();

            // Assert
            testObserver.assertComplete();
            testObserver.assertNoErrors();
            testObserver.assertValue(false);
            testContext.completeNow();
          });
    }

    @Test
    @DisplayName("Should return true when under maintenance")
    void testUnderMaintenance(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      MaintenanceUtil.setMaintenance(vertx);

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<Boolean> testObserver = healthCheckDAO.isUnderMaintenance().test();

            // Assert
            testObserver.assertComplete();
            testObserver.assertNoErrors();
            testObserver.assertValue(true);
            testContext.completeNow();
          });
    }

    @Test
    @DisplayName("Should toggle maintenance mode correctly")
    void testToggleMaintenance(Vertx vertx, VertxTestContext testContext) {
      // Arrange & Act
      vertx.runOnContext(
          v -> {
            MaintenanceUtil.clearMaintenance(vertx);
            TestObserver<Boolean> testObserver1 = healthCheckDAO.isUnderMaintenance().test();
            testObserver1.assertValue(false);

            MaintenanceUtil.setMaintenance(vertx);
            TestObserver<Boolean> testObserver2 = healthCheckDAO.isUnderMaintenance().test();
            testObserver2.assertValue(true);

            MaintenanceUtil.clearMaintenance(vertx);
            TestObserver<Boolean> testObserver3 = healthCheckDAO.isUnderMaintenance().test();
            testObserver3.assertValue(false);

            testContext.completeNow();
          });
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should check all health indicators successfully")
    void testAllHealthChecksSuccess(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(true));
      when(aerospikeClient.isConnected()).thenReturn(Single.just(true));
      MaintenanceUtil.clearMaintenance(vertx);

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<Boolean> mysqlObserver = healthCheckDAO.isMySQLReaderConnected().test();
            TestObserver<Boolean> aerospikeObserver = healthCheckDAO.isAerospikeConnected().test();
            TestObserver<Boolean> maintenanceObserver = healthCheckDAO.isUnderMaintenance().test();

            // Assert
            mysqlObserver.assertComplete().assertNoErrors().assertValue(true);
            aerospikeObserver.assertComplete().assertNoErrors().assertValue(true);
            maintenanceObserver.assertComplete().assertNoErrors().assertValue(false);

            verify(mySQLReaderClient, times(1)).isConnected();
            verify(aerospikeClient, times(1)).isConnected();
            testContext.completeNow();
          });
    }

    @Test
    @DisplayName("Should handle all connections down")
    void testAllConnectionsDown(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(false));
      when(aerospikeClient.isConnected()).thenReturn(Single.just(false));

      // Act
      TestObserver<Boolean> mysqlObserver = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> aerospikeObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      mysqlObserver.assertComplete().assertNoErrors().assertValue(false);
      aerospikeObserver.assertComplete().assertNoErrors().assertValue(false);

      verify(mySQLReaderClient, times(1)).isConnected();
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle partial connectivity")
    void testPartialConnectivity(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(true));
      when(aerospikeClient.isConnected()).thenReturn(Single.just(false));

      // Act
      TestObserver<Boolean> mysqlObserver = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> aerospikeObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      mysqlObserver.assertComplete().assertNoErrors().assertValue(true);
      aerospikeObserver.assertComplete().assertNoErrors().assertValue(false);

      verify(mySQLReaderClient, times(1)).isConnected();
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle mixed errors and successes")
    void testMixedErrorsAndSuccesses(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("MySQL error")));
      when(aerospikeClient.isConnected()).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> mysqlObserver = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> aerospikeObserver = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      mysqlObserver.assertComplete().assertNoErrors().assertValue(false);
      aerospikeObserver.assertComplete().assertNoErrors().assertValue(true);

      verify(mySQLReaderClient, times(1)).isConnected();
      verify(aerospikeClient, times(1)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle health check during maintenance")
    void testHealthCheckDuringMaintenance(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(true));
      when(aerospikeClient.isConnected()).thenReturn(Single.just(true));
      MaintenanceUtil.setMaintenance(vertx);

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<Boolean> mysqlObserver = healthCheckDAO.isMySQLReaderConnected().test();
            TestObserver<Boolean> aerospikeObserver = healthCheckDAO.isAerospikeConnected().test();
            TestObserver<Boolean> maintenanceObserver = healthCheckDAO.isUnderMaintenance().test();

            // Assert
            mysqlObserver.assertComplete().assertNoErrors().assertValue(true);
            aerospikeObserver.assertComplete().assertNoErrors().assertValue(true);
            maintenanceObserver.assertComplete().assertNoErrors().assertValue(true);
            testContext.completeNow();
          });
    }
  }

  @Nested
  @DisplayName("Multiple Invocation Tests")
  class MultipleInvocationTests {

    @Test
    @DisplayName("Should handle multiple MySQL connection checks")
    void testMultipleMySQLChecks(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> observer1 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> observer2 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> observer3 = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(true);
      observer2.assertComplete().assertNoErrors().assertValue(true);
      observer3.assertComplete().assertNoErrors().assertValue(true);
      verify(mySQLReaderClient, times(3)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple Aerospike connection checks")
    void testMultipleAerospikeChecks(VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.isConnected()).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> observer1 = healthCheckDAO.isAerospikeConnected().test();
      TestObserver<Boolean> observer2 = healthCheckDAO.isAerospikeConnected().test();
      TestObserver<Boolean> observer3 = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(true);
      observer2.assertComplete().assertNoErrors().assertValue(true);
      observer3.assertComplete().assertNoErrors().assertValue(true);
      verify(aerospikeClient, times(3)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle connection state changes")
    void testConnectionStateChanges(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected())
          .thenReturn(Single.just(true))
          .thenReturn(Single.just(false))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> observer1 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> observer2 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> observer3 = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(true);
      observer2.assertComplete().assertNoErrors().assertValue(false);
      observer3.assertComplete().assertNoErrors().assertValue(true);
      verify(mySQLReaderClient, times(3)).isConnected();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Error Recovery Tests")
  class ErrorRecoveryTests {

    @Test
    @DisplayName("Should recover from transient MySQL error")
    void testRecoverFromMySQLError(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("Transient error")))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> observer1 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> observer2 = healthCheckDAO.isMySQLReaderConnected().test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(false);
      observer2.assertComplete().assertNoErrors().assertValue(true);
      verify(mySQLReaderClient, times(2)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should recover from transient Aerospike error")
    void testRecoverFromAerospikeError(VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("Transient error")))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> observer1 = healthCheckDAO.isAerospikeConnected().test();
      TestObserver<Boolean> observer2 = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(false);
      observer2.assertComplete().assertNoErrors().assertValue(true);
      verify(aerospikeClient, times(2)).isConnected();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle persistent errors gracefully")
    void testPersistentErrors(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("Persistent error")));
      when(aerospikeClient.isConnected())
          .thenReturn(Single.error(new RuntimeException("Persistent error")));

      // Act
      TestObserver<Boolean> mysqlObserver1 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> mysqlObserver2 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> aerospikeObserver1 = healthCheckDAO.isAerospikeConnected().test();
      TestObserver<Boolean> aerospikeObserver2 = healthCheckDAO.isAerospikeConnected().test();

      // Assert - Should always return false on error, not propagate the error
      mysqlObserver1.assertComplete().assertNoErrors().assertValue(false);
      mysqlObserver2.assertComplete().assertNoErrors().assertValue(false);
      aerospikeObserver1.assertComplete().assertNoErrors().assertValue(false);
      aerospikeObserver2.assertComplete().assertNoErrors().assertValue(false);

      verify(mySQLReaderClient, times(2)).isConnected();
      verify(aerospikeClient, times(2)).isConnected();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Concurrency Tests")
  class ConcurrencyTests {

    @Test
    @DisplayName("Should handle concurrent health checks")
    void testConcurrentHealthChecks(VertxTestContext testContext) {
      // Arrange
      when(mySQLReaderClient.isConnected()).thenReturn(Single.just(true));
      when(aerospikeClient.isConnected()).thenReturn(Single.just(true));

      // Act - Simulate concurrent calls
      TestObserver<Boolean> mysql1 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> aerospike1 = healthCheckDAO.isAerospikeConnected().test();
      TestObserver<Boolean> mysql2 = healthCheckDAO.isMySQLReaderConnected().test();
      TestObserver<Boolean> aerospike2 = healthCheckDAO.isAerospikeConnected().test();

      // Assert
      mysql1.assertComplete().assertNoErrors().assertValue(true);
      aerospike1.assertComplete().assertNoErrors().assertValue(true);
      mysql2.assertComplete().assertNoErrors().assertValue(true);
      aerospike2.assertComplete().assertNoErrors().assertValue(true);

      verify(mySQLReaderClient, times(2)).isConnected();
      verify(aerospikeClient, times(2)).isConnected();
      testContext.completeNow();
    }
  }
}
