package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object interface for Experiment operations.
 *
 * <p>Provides methods for creating and updating experiments in the database.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentDAO {

  /**
   * Creates a new experiment in the database.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier for partitioning
   * @param request experiment creation request with all experiment details
   * @return Single emitting 1L on success, 0L on failure
   */
  Single<Long> create(UUID tenantId, UUID projectKey, CreateExperimentRequest request);

  /**
   * Updates experiment fields partially based on provided request map.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> updatePartial(UUID projectKey, UUID experimentId, Map<String, Object> request);
}
