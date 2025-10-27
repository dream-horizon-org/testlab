package com.ascend.testlab.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.ExperimentNameValidationDAO;
import com.ascend.testlab.dto.response.ValidateExperimentNameResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExperimentNameValidationServiceImplTest {

  @Mock private ExperimentNameValidationDAO experimentNameValidationDAO;

  private ExperimentNameValidationServiceImpl experimentNameValidationService;

  private final UUID testProjectId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private final String testExperimentName = "my-test-experiment";

  @BeforeEach
  void setUp() {
    experimentNameValidationService =
        new ExperimentNameValidationServiceImpl(experimentNameValidationDAO);
  }

  @Test
  void testValidateExperimentName_NameAvailable() {
    // Given
    when(experimentNameValidationDAO.isExperimentNameExists(testProjectId, testExperimentName))
        .thenReturn(Single.just(false));

    // When
    Single<ValidateExperimentNameResponse> result =
        experimentNameValidationService.validateExperimentName(testProjectId, testExperimentName);

    // Then
    ValidateExperimentNameResponse response = result.blockingGet();
    assertNotNull(response);
    assertTrue(response.isAvailable());
    assertTrue(response.message().contains("is available"));
    verify(experimentNameValidationDAO, times(1))
        .isExperimentNameExists(testProjectId, testExperimentName);
  }

  @Test
  void testValidateExperimentName_NameNotAvailable() {
    // Given
    when(experimentNameValidationDAO.isExperimentNameExists(testProjectId, testExperimentName))
        .thenReturn(Single.just(true));

    // When
    Single<ValidateExperimentNameResponse> result =
        experimentNameValidationService.validateExperimentName(testProjectId, testExperimentName);

    // Then
    ValidateExperimentNameResponse response = result.blockingGet();
    assertNotNull(response);
    assertFalse(response.isAvailable());
    assertTrue(response.message().contains("already exists"));
    verify(experimentNameValidationDAO, times(1))
        .isExperimentNameExists(testProjectId, testExperimentName);
  }

  @Test
  void testValidateExperimentName_DAOError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database error");
    when(experimentNameValidationDAO.isExperimentNameExists(testProjectId, testExperimentName))
        .thenReturn(Single.error(dbException));

    // When
    Single<ValidateExperimentNameResponse> result =
        experimentNameValidationService.validateExperimentName(testProjectId, testExperimentName);

    // Then
    Exception exception = assertThrows(RuntimeException.class, result::blockingGet);
    assertTrue(exception instanceof RuntimeException);
    assertEquals("Database error", exception.getMessage());
    verify(experimentNameValidationDAO, times(1))
        .isExperimentNameExists(testProjectId, testExperimentName);
  }
}
