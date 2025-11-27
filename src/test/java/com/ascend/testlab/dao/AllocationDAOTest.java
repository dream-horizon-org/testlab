package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.config.AerospikeConfig;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.impl.AllocationDAOImpl;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("AllocationDAO Tests")
class AllocationDAOTest {

  @Mock private PgReaderClient pgReaderClient;
  @Mock private AerospikeClient aerospikeClient;
  @Mock private AerospikeConfig aerospikeConfig;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private AllocationDAO allocationDAO;

  private static final String PROJECT_KEY = "123e4567-e89b-12d3-a456-426614174000";
  private static final UUID EXPERIMENT_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    // Setup common Aerospike config mocks
    lenient().when(aerospikeConfig.getNamespace()).thenReturn("test");
    lenient().when(aerospikeConfig.getUserLockSet()).thenReturn("user_locks");
    lenient().when(aerospikeConfig.getUserAllocationLockBin()).thenReturn("lock");
    lenient().when(aerospikeConfig.getUserAllocationsSet()).thenReturn("user_allocations");
    lenient().when(aerospikeConfig.getAllocationMapBin()).thenReturn("allocations");
    lenient().when(aerospikeConfig.getVariantCountSet()).thenReturn("variant_counts");
    lenient().when(aerospikeConfig.getVariantCountBin()).thenReturn("count");

    this.allocationDAO =
        new AllocationDAOImpl(pgReaderClient, aerospikeClient, aerospikeConfig, objectMapper);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create DAO with valid dependencies")
    void testConstructorWithValidDependencies(VertxTestContext testContext) {
      // Act
      AllocationDAO dao =
          new AllocationDAOImpl(pgReaderClient, aerospikeClient, aerospikeConfig, objectMapper);

      // Assert
      assertNotNull(dao);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("FetchActiveExperiments Tests")
  class FetchActiveExperimentsTests {

    @Test
    @DisplayName("Should fetch active experiments successfully")
    void testFetchActiveExperimentsSuccess(VertxTestContext testContext) {
      // Arrange
      List<String> experimentKeys = List.of("exp1", "exp2");
      List<Experiment> expectedExperiments = List.of(createMockExperiment());

      when(pgReaderClient.fetchAll(
              eq(ReadQuery.GET_EXPERIMENTS_FROM_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(expectedExperiments));

      // Act
      TestObserver<List<Experiment>> testObserver =
          allocationDAO.fetchActiveExperiments(PROJECT_KEY, experimentKeys).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.GET_EXPERIMENTS_FROM_KEY), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle empty experiment list")
    void testFetchActiveExperimentsEmpty(VertxTestContext testContext) {
      // Arrange
      List<String> experimentKeys = List.of();

      when(pgReaderClient.fetchAll(
              eq(ReadQuery.GET_EXPERIMENTS_FROM_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      TestObserver<List<Experiment>> testObserver =
          allocationDAO.fetchActiveExperiments(PROJECT_KEY, experimentKeys).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(List::isEmpty);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle database error")
    void testFetchActiveExperimentsError(VertxTestContext testContext) {
      // Arrange
      List<String> experimentKeys = List.of("exp1");
      RuntimeException expectedException = new RuntimeException("Database error");

      when(pgReaderClient.fetchAll(
              eq(ReadQuery.GET_EXPERIMENTS_FROM_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<List<Experiment>> testObserver =
          allocationDAO.fetchActiveExperiments(PROJECT_KEY, experimentKeys).test();

      // Assert
      testObserver.assertValue(List::isEmpty);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("FetchActiveExperiment Tests")
  class FetchActiveExperimentTests {

    @Test
    @DisplayName("Should fetch active experiment successfully")
    void testFetchActiveExperimentSuccess(VertxTestContext testContext) {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();

      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_LIVE_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          allocationDAO.fetchActiveExperiment(PROJECT_KEY, EXPERIMENT_ID.toString()).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_LIVE_EXPERIMENT), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle experiment not found")
    void testFetchActiveExperimentNotFound(VertxTestContext testContext) {
      // Arrange
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_LIVE_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.empty());

      // Act
      TestObserver<Experiment> testObserver =
          allocationDAO.fetchActiveExperiment(PROJECT_KEY, EXPERIMENT_ID.toString()).test();

      // Assert
      testObserver.assertError(Exception.class);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle database error")
    void testFetchActiveExperimentError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Database error");

      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_LIVE_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.error(expectedException));

      // Act
      TestObserver<Experiment> testObserver =
          allocationDAO.fetchActiveExperiment(PROJECT_KEY, EXPERIMENT_ID.toString()).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("FetchConcludedExperiments Tests")
  class FetchConcludedExperimentsTests {

    @Test
    @DisplayName("Should fetch concluded experiments successfully")
    void testFetchConcludedExperimentsSuccess(VertxTestContext testContext) {
      // Arrange
      List<Experiment> expectedExperiments = List.of(createMockExperiment());

      when(pgReaderClient.fetchAll(
              eq(ReadQuery.GET_CONCLUDED_EXPERIMENTS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(expectedExperiments));

      // Act
      TestObserver<List<Experiment>> testObserver =
          allocationDAO.fetchConcludedExperiments(PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle empty concluded experiments")
    void testFetchConcludedExperimentsEmpty(VertxTestContext testContext) {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.GET_CONCLUDED_EXPERIMENTS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      TestObserver<List<Experiment>> testObserver =
          allocationDAO.fetchConcludedExperiments(PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(List::isEmpty);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle database error and return empty list")
    void testFetchConcludedExperimentsError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Database error");

      when(pgReaderClient.fetchAll(
              eq(ReadQuery.GET_CONCLUDED_EXPERIMENTS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<List<Experiment>> testObserver =
          allocationDAO.fetchConcludedExperiments(PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(List::isEmpty);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("AcquireUserLock Tests")
  class AcquireUserLockTests {

    @Test
    @DisplayName("Should acquire lock successfully")
    void testAcquireUserLockSuccess(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";

      lenient()
          .when(aerospikeClient.put(any(WritePolicy.class), any(Key.class), any()))
          .thenReturn(Single.just(mock(Key.class)));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.acquireUserLock(userId, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false on error")
    void testAcquireUserLockError(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";

      RuntimeException expectedException = new RuntimeException("Aerospike error");
      when(aerospikeClient.put(any(WritePolicy.class), any(Key.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.acquireUserLock(userId, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("ReleaseUserLock Tests")
  class ReleaseUserLockTests {

    @Test
    @DisplayName("Should release lock successfully")
    void testReleaseUserLockSuccess(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";

      when(aerospikeClient.delete(any(WritePolicy.class), any(Key.class)))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.releaseUserLock(userId, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return true even on error")
    void testReleaseUserLockError(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";

      RuntimeException expectedException = new RuntimeException("Aerospike error");
      when(aerospikeClient.delete(any(WritePolicy.class), any(Key.class)))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.releaseUserLock(userId, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("CheckThreshold Tests")
  class CheckThresholdTests {

    @Test
    @DisplayName("Should return true when under threshold")
    void testCheckThresholdUnderLimit(VertxTestContext testContext) {
      // Arrange
      Experiment experiment =
          Experiment.builder()
              .experimentId(EXPERIMENT_ID)
              .projectKey(PROJECT_KEY)
              .name("Test Experiment")
              .threshold(1000L)
              .variants(
                  Map.of(
                      "control", Variant.builder().displayName("Control").build(),
                      "treatment", Variant.builder().displayName("Treatment").build()))
              .build();

      // Mock Aerospike to return records with counts
      Record record1 = mock(Record.class);
      Record record2 = mock(Record.class);
      lenient().when(record1.getLong("count")).thenReturn(50L);
      lenient().when(record2.getLong("count")).thenReturn(30L);

      List<Record> records = Arrays.asList(record1, record2);
      lenient()
          .when(aerospikeClient.get(any(BatchPolicy.class), anyList(), eq("count")))
          .thenReturn(Single.just(records));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.checkThreshold(PROJECT_KEY, experiment).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return true when threshold is zero (unlimited)")
    void testCheckThresholdZero(VertxTestContext testContext) {
      // Arrange
      Experiment experiment =
          Experiment.builder()
              .experimentId(EXPERIMENT_ID)
              .projectKey(PROJECT_KEY)
              .name("Test Experiment")
              .threshold(0L) // 0 means unlimited
              .variants(
                  Map.of(
                      "control", Variant.builder().displayName("Control").build(),
                      "treatment", Variant.builder().displayName("Treatment").build()))
              .build();

      // Mock Aerospike to return high counts
      Record record1 = mock(Record.class);
      Record record2 = mock(Record.class);
      lenient().when(record1.getLong("count")).thenReturn(500L);
      lenient().when(record2.getLong("count")).thenReturn(600L);

      List<Record> records = Arrays.asList(record1, record2);
      lenient()
          .when(aerospikeClient.get(any(BatchPolicy.class), anyList(), eq("count")))
          .thenReturn(Single.just(records));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.checkThreshold(PROJECT_KEY, experiment).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true); // Should be true because threshold 0 means unlimited
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when threshold is exceeded")
    void testCheckThresholdExceeded(VertxTestContext testContext) {
      // Arrange
      Experiment experiment =
          Experiment.builder()
              .experimentId(EXPERIMENT_ID)
              .projectKey(PROJECT_KEY)
              .name("Test Experiment")
              .threshold(100L)
              .variants(
                  Map.of(
                      "control", Variant.builder().displayName("Control").build(),
                      "treatment", Variant.builder().displayName("Treatment").build()))
              .build();

      // Mock Aerospike to return counts that exceed threshold
      Record record1 = mock(Record.class);
      Record record2 = mock(Record.class);
      lenient().when(record1.getLong("count")).thenReturn(60L);
      lenient().when(record2.getLong("count")).thenReturn(50L);

      List<Record> records = Arrays.asList(record1, record2);
      lenient()
          .when(aerospikeClient.get(any(BatchPolicy.class), anyList(), eq("count")))
          .thenReturn(Single.just(records));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.checkThreshold(PROJECT_KEY, experiment).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false); // Should be false because 110 >= 100
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle Aerospike error gracefully")
    void testCheckThresholdError(VertxTestContext testContext) {
      // Arrange
      Experiment experiment =
          Experiment.builder()
              .experimentId(EXPERIMENT_ID)
              .projectKey(PROJECT_KEY)
              .name("Test Experiment")
              .threshold(1000L)
              .variants(Map.of("control", Variant.builder().displayName("Control").build()))
              .build();

      RuntimeException expectedException = new RuntimeException("Aerospike error");
      lenient()
          .when(aerospikeClient.get(any(BatchPolicy.class), anyList(), eq("count")))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO.checkThreshold(PROJECT_KEY, experiment).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("GetAllocations Single User Tests")
  class GetAllocationsTests {

    @Test
    @DisplayName("Should return empty list when user has no allocations")
    void testGetAllocationsEmpty(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";

      // Mock a record with null map (simulating no allocations)
      Record emptyRecord = mock(Record.class);
      when(emptyRecord.getMap(anyString())).thenReturn(null);

      when(aerospikeClient.get(any(Policy.class), any(Key.class), anyString()))
          .thenReturn(Single.just(emptyRecord));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationDAO.getAllocations(userId, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(List::isEmpty);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error from Aerospike")
    void testGetAllocationsError(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";

      RuntimeException expectedException = new RuntimeException("Aerospike error");
      when(aerospikeClient.get(any(Policy.class), any(Key.class), anyString()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationDAO.getAllocations(userId, PROJECT_KEY).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("GetAllocations Batch Tests")
  class GetAllocationsBatchTests {

    @Test
    @DisplayName("Should return empty map when no user IDs provided")
    void testGetAllocationsBatchEmpty(VertxTestContext testContext) {
      // Act
      TestObserver<Map<String, List<UserExperimentMap>>> testObserver =
          allocationDAO.getAllocations(Collections.emptyList(), PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(Map::isEmpty);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty map when user IDs list contains null")
    void testGetAllocationsBatchWithNullId(VertxTestContext testContext) {
      // Arrange
      List<String> userIds = new ArrayList<>();
      userIds.add(null);

      List<Record> records = Arrays.asList((Record) null);
      when(aerospikeClient.get(any(BatchPolicy.class), anyList(), anyString()))
          .thenReturn(Single.just(records));

      // Act
      TestObserver<Map<String, List<UserExperimentMap>>> testObserver =
          allocationDAO.getAllocations(userIds, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should fetch allocations for multiple users")
    void testGetAllocationsBatchSuccess(VertxTestContext testContext) {
      // Arrange
      List<String> userIds = List.of("user1", "user2");

      List<Record> records = Arrays.asList(null, null);
      lenient()
          .when(aerospikeClient.get(any(BatchPolicy.class), anyList(), anyString()))
          .thenReturn(Single.just(records));

      // Act
      TestObserver<Map<String, List<UserExperimentMap>>> testObserver =
          allocationDAO.getAllocations(userIds, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(result -> result.size() == 2);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error from batch operation")
    void testGetAllocationsBatchError(VertxTestContext testContext) {
      // Arrange
      List<String> userIds = List.of("user1", "user2");

      RuntimeException expectedException = new RuntimeException("Aerospike batch error");
      lenient()
          .when(aerospikeClient.get(any(BatchPolicy.class), anyList(), anyString()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Map<String, List<UserExperimentMap>>> testObserver =
          allocationDAO.getAllocations(userIds, PROJECT_KEY).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("ReallocateUserVariant Tests")
  class ReallocateUserVariantTests {

    @Test
    @DisplayName("Should reallocate user to new variant successfully")
    void testReallocateUserVariantSuccess(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";
      String oldVariant = "control";
      String newVariant = "treatment";

      UserExperimentMap newAssignment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(newVariant)
              .assignedAt(System.currentTimeMillis())
              .status("REALLOCATED")
              .build();

      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .userId(userId)
              .variantName(newVariant)
              .reason("Testing reallocation")
              .build();

      // Mock variant count operations
      Record variantRecord = mock(Record.class);
      when(variantRecord.getLong(anyString())).thenReturn(10L);
      when(aerospikeClient.operate(any(WritePolicy.class), any(Key.class), any(), any()))
          .thenReturn(Single.just(variantRecord));

      // Mock user assignment update
      when(aerospikeClient.operate(any(WritePolicy.class), any(Key.class), any(Operation.class)))
          .thenReturn(Single.just(mock(Record.class)));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationDAO
              .reallocateUserVariant(PROJECT_KEY, oldVariant, newAssignment, request)
              .test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(result -> result.getVariantName().equals(newVariant));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when updating user assignment")
    void testReallocateUserVariantAssignmentError(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";
      String oldVariant = "control";
      String newVariant = "treatment";

      UserExperimentMap newAssignment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(newVariant)
              .assignedAt(System.currentTimeMillis())
              .status("REALLOCATED")
              .build();

      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .userId(userId)
              .variantName(newVariant)
              .reason("Testing reallocation")
              .build();

      // Mock variant count operations success
      Record variantRecord = mock(Record.class);
      when(variantRecord.getLong(anyString())).thenReturn(10L);
      when(aerospikeClient.operate(any(WritePolicy.class), any(Key.class), any(), any()))
          .thenReturn(Single.just(variantRecord));

      // Mock user assignment update failure
      when(aerospikeClient.operate(any(WritePolicy.class), any(Key.class), any(Operation.class)))
          .thenReturn(Single.error(new RuntimeException("Aerospike error")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationDAO
              .reallocateUserVariant(PROJECT_KEY, oldVariant, newAssignment, request)
              .test();

      // Assert
      testObserver.assertError(Exception.class);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when updating variant counts")
    void testReallocateUserVariantCountError(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";
      String oldVariant = "control";
      String newVariant = "treatment";

      UserExperimentMap newAssignment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(newVariant)
              .assignedAt(System.currentTimeMillis())
              .status("REALLOCATED")
              .build();

      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .userId(userId)
              .variantName(newVariant)
              .reason("Testing reallocation")
              .build();

      // Mock variant count operations failure
      when(aerospikeClient.operate(any(WritePolicy.class), any(Key.class), any(), any()))
          .thenReturn(Single.error(new RuntimeException("Variant count error")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationDAO
              .reallocateUserVariant(PROJECT_KEY, oldVariant, newAssignment, request)
              .test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("InsertAllocationsAndIncrementCounts Tests")
  class InsertAllocationsAndIncrementCountsTests {

    @Test
    @DisplayName("Should return true when assignments are empty")
    void testInsertAllocationsEmpty(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";
      List<UserExperimentMap> assignments = List.of();
      Map<String, String> variantCountMap = Map.of();

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO
              .insertAllocationsAndIncrementCounts(
                  userId, PROJECT_KEY, assignments, variantCountMap)
              .test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return true when assignments are null")
    void testInsertAllocationsNull(VertxTestContext testContext) {
      // Arrange
      String userId = "user123";
      Map<String, String> variantCountMap = Map.of();

      // Act
      TestObserver<Boolean> testObserver =
          allocationDAO
              .insertAllocationsAndIncrementCounts(userId, PROJECT_KEY, null, variantCountMap)
              .test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      testContext.completeNow();
    }
  }

  // Helper methods
  private Experiment createMockExperiment() {
    return Experiment.builder()
        .experimentId(EXPERIMENT_ID)
        .projectKey(PROJECT_KEY)
        .name("Test Experiment")
        .key("test_experiment")
        .status(ExperimentStatus.LIVE)
        .build();
  }
}
