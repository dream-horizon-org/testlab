package com.ascend.testlab.client.aerospike;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.CommitLevel;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.Replica;
import com.aerospike.client.policy.WritePolicy;
import com.ascend.testlab.client.aerospike.impl.AerospikeClientImpl;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for AerospikeClientImpl.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("AerospikeClientImpl Tests")
public class AerospikeClientTest {

  private AerospikeClient aerospikeClient;

  @Mock private io.d11.aerospike.client.AerospikeClient mockClient;

  private static final String TEST_NAMESPACE = "test";
  private static final String TEST_SET = "testSet";
  private static final String TEST_KEY_VALUE = "testKey";
  private static final String TEST_BIN_NAME = "testBin";
  private static final String TEST_BIN_VALUE = "testValue";

  @BeforeEach
  void setUp(io.vertx.core.Vertx coreVertx) {
    this.aerospikeClient = new AerospikeClientImpl(Vertx.newInstance(coreVertx), mockClient);
  }

  @Nested
  @DisplayName("Connection Management Tests")
  class ConnectionManagementTests {

    @Test
    @DisplayName("Should close client successfully")
    void testCloseSuccess(VertxTestContext testContext) {
      // Arrange
      doNothing().when(mockClient).close();

      // Act
      TestObserver<Void> testObserver = aerospikeClient.close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(mockClient, times(1)).close();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle close with exception")
    void testCloseWithException(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Close failed");
      doThrow(expectedException).when(mockClient).close();

      // Act
      TestObserver<Void> testObserver = aerospikeClient.close().test();

      // Assert
      testObserver.assertError(expectedException);
      testObserver.assertNotComplete();
      verify(mockClient, times(1)).close();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return true when client is connected")
    void testIsConnectedTrue(VertxTestContext testContext) {
      // Arrange
      doAnswer(
              invocation -> {
                Handler<AsyncResult<Boolean>> handler = invocation.getArgument(0);
                handler.handle(Future.succeededFuture(true));
                return null;
              })
          .when(mockClient)
          .isConnected(any());

      // Act
      TestObserver<Boolean> testObserver = aerospikeClient.isConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      verify(mockClient, times(1)).isConnected(any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when client is not connected")
    void testIsConnectedFalse(VertxTestContext testContext) {
      // Arrange
      doAnswer(
              invocation -> {
                Handler<AsyncResult<Boolean>> handler = invocation.getArgument(0);
                handler.handle(Future.succeededFuture(false));
                return null;
              })
          .when(mockClient)
          .isConnected(any());

      // Act
      TestObserver<Boolean> testObserver = aerospikeClient.isConnected().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(mockClient, times(1)).isConnected(any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle isConnected failure")
    void testIsConnectedFailure(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Connection check failed");
      doAnswer(
              invocation -> {
                Handler<AsyncResult<Boolean>> handler = invocation.getArgument(0);
                handler.handle(Future.failedFuture(expectedException));
                return null;
              })
          .when(mockClient)
          .isConnected(any());

      // Act
      TestObserver<Boolean> testObserver = aerospikeClient.isConnected().test();

      // Assert
      testObserver.assertError(expectedException);
      testObserver.assertNotComplete();
      verify(mockClient, times(1)).isConnected(any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Policy Tests")
  class PolicyTests {

    @Test
    @DisplayName("Should return default policy with correct settings")
    void testGetDefaultPolicy(VertxTestContext testContext) {
      // Act
      Policy policy = aerospikeClient.getDefaultPolicy();

      // Assert
      assertNotNull(policy);
      assertEquals(Replica.MASTER_PROLES, policy.replica);
      assertTrue(policy.sendKey);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return new instance of default policy each time")
    void testGetDefaultPolicyNewInstance(VertxTestContext testContext) {
      // Act
      Policy policy1 = aerospikeClient.getDefaultPolicy();
      Policy policy2 = aerospikeClient.getDefaultPolicy();

      // Assert
      assertNotNull(policy1);
      assertNotNull(policy2);
      assertNotSame(policy1, policy2);
      assertEquals(policy1.replica, policy2.replica);
      assertEquals(policy1.sendKey, policy2.sendKey);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return default write policy with correct settings")
    void testGetDefaultWritePolicy(VertxTestContext testContext) {
      // Act
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();

      // Assert
      assertNotNull(writePolicy);
      assertEquals(Replica.MASTER_PROLES, writePolicy.replica);
      assertTrue(writePolicy.sendKey);
      assertEquals(CommitLevel.COMMIT_ALL, writePolicy.commitLevel);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return new instance of default write policy each time")
    void testGetDefaultWritePolicyNewInstance(VertxTestContext testContext) {
      // Act
      WritePolicy policy1 = aerospikeClient.getDefaultWritePolicy();
      WritePolicy policy2 = aerospikeClient.getDefaultWritePolicy();

      // Assert
      assertNotNull(policy1);
      assertNotNull(policy2);
      assertNotSame(policy1, policy2);
      assertEquals(policy1.replica, policy2.replica);
      assertEquals(policy1.sendKey, policy2.sendKey);
      assertEquals(policy1.commitLevel, policy2.commitLevel);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Get Operation Tests")
  class GetOperationTests {

    @Test
    @DisplayName("Should get record successfully")
    void testGetSuccess(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      Policy policy = aerospikeClient.getDefaultPolicy();
      Map<String, Object> bins = new HashMap<>();
      bins.put(TEST_BIN_NAME, TEST_BIN_VALUE);
      Record expectedRecord = new Record(bins, 1, 0);

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Record>> handler = invocation.getArgument(3);
                handler.handle(Future.succeededFuture(expectedRecord));
                return null;
              })
          .when(mockClient)
          .get(eq(policy), eq(key), any(String[].class), any());

      // Act
      TestObserver<Record> testObserver = aerospikeClient.get(policy, key, TEST_BIN_NAME).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(expectedRecord);
      verify(mockClient, times(1)).get(eq(policy), eq(key), any(String[].class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle get with multiple bin names")
    void testGetMultipleBins(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      Policy policy = aerospikeClient.getDefaultPolicy();
      String[] binNames = {"bin1", "bin2", "bin3"};
      Map<String, Object> bins = new HashMap<>();
      bins.put("bin1", "value1");
      bins.put("bin2", "value2");
      bins.put("bin3", "value3");
      Record expectedRecord = new Record(bins, 1, 0);

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Record>> handler = invocation.getArgument(3);
                handler.handle(Future.succeededFuture(expectedRecord));
                return null;
              })
          .when(mockClient)
          .get(eq(policy), eq(key), any(String[].class), any());

      // Act
      TestObserver<Record> testObserver = aerospikeClient.get(policy, key, binNames).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(expectedRecord);
      verify(mockClient, times(1)).get(eq(policy), eq(key), any(String[].class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle get failure")
    void testGetFailure(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      Policy policy = aerospikeClient.getDefaultPolicy();
      RuntimeException expectedException = new RuntimeException("Get operation failed");

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Record>> handler = invocation.getArgument(3);
                handler.handle(Future.failedFuture(expectedException));
                return null;
              })
          .when(mockClient)
          .get(eq(policy), eq(key), any(String[].class), any());

      // Act
      TestObserver<Record> testObserver = aerospikeClient.get(policy, key, TEST_BIN_NAME).test();

      // Assert
      testObserver.assertError(expectedException);
      testObserver.assertNotComplete();
      verify(mockClient, times(1)).get(eq(policy), eq(key), any(String[].class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Put Operation Tests")
  class PutOperationTests {

    @Test
    @DisplayName("Should put record successfully")
    void testPutSuccess(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      Bin bin = new Bin(TEST_BIN_NAME, TEST_BIN_VALUE);

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Key>> handler = invocation.getArgument(3);
                handler.handle(Future.succeededFuture(key));
                return null;
              })
          .when(mockClient)
          .put(eq(writePolicy), eq(key), any(Bin[].class), any());

      // Act
      TestObserver<Key> testObserver = aerospikeClient.put(writePolicy, key, bin).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(key);
      verify(mockClient, times(1)).put(eq(writePolicy), eq(key), any(Bin[].class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should put record with multiple bins")
    void testPutMultipleBins(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      Bin[] bins = {new Bin("bin1", "value1"), new Bin("bin2", 123), new Bin("bin3", true)};

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Key>> handler = invocation.getArgument(3);
                handler.handle(Future.succeededFuture(key));
                return null;
              })
          .when(mockClient)
          .put(eq(writePolicy), eq(key), any(Bin[].class), any());

      // Act
      TestObserver<Key> testObserver = aerospikeClient.put(writePolicy, key, bins).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(key);
      verify(mockClient, times(1)).put(eq(writePolicy), eq(key), any(Bin[].class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle put failure")
    void testPutFailure(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      Bin bin = new Bin(TEST_BIN_NAME, TEST_BIN_VALUE);
      RuntimeException expectedException = new RuntimeException("Put operation failed");

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Key>> handler = invocation.getArgument(3);
                handler.handle(Future.failedFuture(expectedException));
                return null;
              })
          .when(mockClient)
          .put(eq(writePolicy), eq(key), any(Bin[].class), any());

      // Act
      TestObserver<Key> testObserver = aerospikeClient.put(writePolicy, key, bin).test();

      // Assert
      testObserver.assertError(expectedException);
      testObserver.assertNotComplete();
      verify(mockClient, times(1)).put(eq(writePolicy), eq(key), any(Bin[].class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Operate Operation Tests")
  class OperateOperationTests {

    @Test
    @DisplayName("Should operate successfully")
    void testOperateSuccess(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      Operation operation = Operation.put(new Bin(TEST_BIN_NAME, TEST_BIN_VALUE));
      Map<String, Object> bins = new HashMap<>();
      bins.put(TEST_BIN_NAME, TEST_BIN_VALUE);
      Record expectedRecord = new Record(bins, 1, 0);

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Record>> handler = invocation.getArgument(3);
                handler.handle(Future.succeededFuture(expectedRecord));
                return null;
              })
          .when(mockClient)
          .operate(eq(writePolicy), eq(key), any(Operation[].class), any());

      // Act
      TestObserver<Record> testObserver =
          aerospikeClient.operate(writePolicy, key, operation).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(expectedRecord);
      verify(mockClient, times(1)).operate(eq(writePolicy), eq(key), any(Operation[].class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should operate with multiple operations")
    void testOperateMultipleOperations(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      Operation[] operations = {
        Operation.put(new Bin("bin1", "value1")),
        Operation.get("bin2"),
        Operation.add(new Bin("bin3", 10))
      };
      Map<String, Object> bins = new HashMap<>();
      bins.put("bin1", "value1");
      bins.put("bin2", "value2");
      bins.put("bin3", 20);
      Record expectedRecord = new Record(bins, 1, 0);

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Record>> handler = invocation.getArgument(3);
                handler.handle(Future.succeededFuture(expectedRecord));
                return null;
              })
          .when(mockClient)
          .operate(eq(writePolicy), eq(key), any(Operation[].class), any());

      // Act
      TestObserver<Record> testObserver =
          aerospikeClient.operate(writePolicy, key, operations).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(expectedRecord);
      verify(mockClient, times(1)).operate(eq(writePolicy), eq(key), any(Operation[].class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle operate failure")
    void testOperateFailure(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      Operation operation = Operation.put(new Bin(TEST_BIN_NAME, TEST_BIN_VALUE));
      RuntimeException expectedException = new RuntimeException("Operate operation failed");

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Record>> handler = invocation.getArgument(3);
                handler.handle(Future.failedFuture(expectedException));
                return null;
              })
          .when(mockClient)
          .operate(eq(writePolicy), eq(key), any(Operation[].class), any());

      // Act
      TestObserver<Record> testObserver =
          aerospikeClient.operate(writePolicy, key, operation).test();

      // Assert
      testObserver.assertError(expectedException);
      testObserver.assertNotComplete();
      verify(mockClient, times(1)).operate(eq(writePolicy), eq(key), any(Operation[].class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Delete Operation Tests")
  class DeleteOperationTests {

    @Test
    @DisplayName("Should delete record successfully when exists")
    void testDeleteSuccess(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Boolean>> handler = invocation.getArgument(2);
                handler.handle(Future.succeededFuture(true));
                return null;
              })
          .when(mockClient)
          .delete(eq(writePolicy), eq(key), any());

      // Act
      TestObserver<Boolean> testObserver = aerospikeClient.delete(writePolicy, key).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      verify(mockClient, times(1)).delete(eq(writePolicy), eq(key), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when deleting non-existent record")
    void testDeleteNonExistentKey(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, "nonExistentKey");
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Boolean>> handler = invocation.getArgument(2);
                handler.handle(Future.succeededFuture(false));
                return null;
              })
          .when(mockClient)
          .delete(eq(writePolicy), eq(key), any());

      // Act
      TestObserver<Boolean> testObserver = aerospikeClient.delete(writePolicy, key).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      verify(mockClient, times(1)).delete(eq(writePolicy), eq(key), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle delete failure")
    void testDeleteFailure(VertxTestContext testContext) {
      // Arrange
      Key key = new Key(TEST_NAMESPACE, TEST_SET, TEST_KEY_VALUE);
      WritePolicy writePolicy = aerospikeClient.getDefaultWritePolicy();
      RuntimeException expectedException = new RuntimeException("Delete operation failed");

      doAnswer(
              invocation -> {
                Handler<AsyncResult<Boolean>> handler = invocation.getArgument(2);
                handler.handle(Future.failedFuture(expectedException));
                return null;
              })
          .when(mockClient)
          .delete(eq(writePolicy), eq(key), any());

      // Act
      TestObserver<Boolean> testObserver = aerospikeClient.delete(writePolicy, key).test();

      // Assert
      testObserver.assertError(expectedException);
      testObserver.assertNotComplete();
      verify(mockClient, times(1)).delete(eq(writePolicy), eq(key), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Policy Immutability Tests")
  class PolicyImmutabilityTests {

    @Test
    @DisplayName("Should not affect default policy when modified")
    void testDefaultPolicyImmutability(VertxTestContext testContext) {
      // Arrange & Act
      Policy policy1 = aerospikeClient.getDefaultPolicy();
      policy1.replica = Replica.SEQUENCE;
      policy1.sendKey = false;

      Policy policy2 = aerospikeClient.getDefaultPolicy();

      // Assert
      assertEquals(Replica.MASTER_PROLES, policy2.replica);
      assertTrue(policy2.sendKey);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should not affect default write policy when modified")
    void testDefaultWritePolicyImmutability(VertxTestContext testContext) {
      // Arrange & Act
      WritePolicy policy1 = aerospikeClient.getDefaultWritePolicy();
      policy1.replica = Replica.SEQUENCE;
      policy1.sendKey = false;
      policy1.commitLevel = CommitLevel.COMMIT_MASTER;

      WritePolicy policy2 = aerospikeClient.getDefaultWritePolicy();

      // Assert
      assertEquals(Replica.MASTER_PROLES, policy2.replica);
      assertTrue(policy2.sendKey);
      assertEquals(CommitLevel.COMMIT_ALL, policy2.commitLevel);
      testContext.completeNow();
    }
  }
}
