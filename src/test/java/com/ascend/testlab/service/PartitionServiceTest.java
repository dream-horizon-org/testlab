package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.enums.PartitionStatus;
import com.ascend.testlab.dao.PartitionDAO;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.ascend.testlab.service.impl.PartitionServiceImpl;
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
 * Comprehensive unit tests for PartitionServiceImpl.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("PartitionService Tests")
public class PartitionServiceTest {

  @Mock private PartitionDAO partitionDAO;
  private PartitionService partitionService;

  private static final String PROJECT_KEY = "test-project-key";
  private static final String USER_ID = "test-user";

  @BeforeEach
  void setUp() {
    partitionService = new PartitionServiceImpl(partitionDAO);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create service with valid DAO")
    void testConstructorWithValidDAO() {
      // Act
      PartitionServiceImpl service = new PartitionServiceImpl(partitionDAO);

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should create service with null DAO")
    void testConstructorWithNullDAO() {
      // Act - Constructor doesn't validate null, but will fail at runtime
      PartitionServiceImpl service = new PartitionServiceImpl(null);

      // Assert
      assertNotNull(service);
    }
  }

  @Nested
  @DisplayName("Create Project Partition Tests")
  class CreateProjectPartitionTests {

    @Test
    @DisplayName("Should return success response when partition is created successfully")
    void testCreateProjectPartition_Success() {
      // Arrange
      PartitionResponse mockResponse =
          PartitionResponse.builder()
              .projectKey(PROJECT_KEY)
              .status(PartitionStatus.SUCCESS.name())
              .message("Partitions created successfully")
              .build();
      when(partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID))
          .thenReturn(Single.just(mockResponse));

      // Act
      TestObserver<PartitionResponse> testObserver =
          partitionService.createProjectPartition(PROJECT_KEY, USER_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      PartitionResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(PROJECT_KEY, response.getProjectKey());
      assertEquals(PartitionStatus.SUCCESS.name(), response.getStatus());
      assertEquals("Partitions created successfully", response.getMessage());
      verify(partitionDAO, times(1)).createProjectPartition(PROJECT_KEY, USER_ID);
    }

    @Test
    @DisplayName("Should return idempotent response when partition already exists")
    void testCreateProjectPartition_Idempotent() {
      // Arrange
      PartitionResponse mockResponse =
          PartitionResponse.builder()
              .projectKey(PROJECT_KEY)
              .status(PartitionStatus.SUCCESS.name())
              .message("Partitions already exist")
              .build();
      when(partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID))
          .thenReturn(Single.just(mockResponse));

      // Act
      TestObserver<PartitionResponse> testObserver =
          partitionService.createProjectPartition(PROJECT_KEY, USER_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      PartitionResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(PROJECT_KEY, response.getProjectKey());
      assertEquals(PartitionStatus.SUCCESS.name(), response.getStatus());
      assertEquals("Partitions already exist", response.getMessage());
      verify(partitionDAO, times(1)).createProjectPartition(PROJECT_KEY, USER_ID);
    }

    @Test
    @DisplayName("Should throw RestException when DAO fails")
    void testCreateProjectPartition_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID))
          .thenReturn(Single.error(dbException));

      // Act
      Single<PartitionResponse> result =
          partitionService.createProjectPartition(PROJECT_KEY, USER_ID);
      TestObserver<PartitionResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(partitionDAO, times(1)).createProjectPartition(PROJECT_KEY, USER_ID);
    }

    @Test
    @DisplayName("Should handle different project keys correctly")
    void testCreateProjectPartition_DifferentProjectKeys() {
      // Arrange
      String projectKey1 = "project-key-1";
      String projectKey2 = "project-key-2";

      PartitionResponse response1 =
          PartitionResponse.builder()
              .projectKey(projectKey1)
              .status(PartitionStatus.SUCCESS.name())
              .message("Partitions created successfully")
              .build();
      PartitionResponse response2 =
          PartitionResponse.builder()
              .projectKey(projectKey2)
              .status(PartitionStatus.SUCCESS.name())
              .message("Partitions created successfully")
              .build();

      when(partitionDAO.createProjectPartition(projectKey1, USER_ID))
          .thenReturn(Single.just(response1));
      when(partitionDAO.createProjectPartition(projectKey2, USER_ID))
          .thenReturn(Single.just(response2));

      // Act
      TestObserver<PartitionResponse> testObserver1 =
          partitionService.createProjectPartition(projectKey1, USER_ID).test();
      TestObserver<PartitionResponse> testObserver2 =
          partitionService.createProjectPartition(projectKey2, USER_ID).test();

      // Assert
      testObserver1.assertComplete();
      testObserver1.assertNoErrors();
      assertEquals(projectKey1, testObserver1.values().get(0).getProjectKey());

      testObserver2.assertComplete();
      testObserver2.assertNoErrors();
      assertEquals(projectKey2, testObserver2.values().get(0).getProjectKey());

      verify(partitionDAO, times(1)).createProjectPartition(projectKey1, USER_ID);
      verify(partitionDAO, times(1)).createProjectPartition(projectKey2, USER_ID);
    }
  }
}
