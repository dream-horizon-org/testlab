package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.OwnerDAO;
import com.ascend.testlab.service.OwnerService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of OwnerService for owner business logic.
 *
 * <p>This class handles owner operations with proper validation, error handling, and logging.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class OwnerServiceImpl implements OwnerService {

  private final OwnerDAO ownerDAO;

  /**
   * Constructs OwnerServiceImpl with owner DAO.
   *
   * @param ownerDAO owner data access object
   */
  @Inject
  public OwnerServiceImpl(OwnerDAO ownerDAO) {
    this.ownerDAO = ownerDAO;
  }

  /**
   * Inserts an owner for an experiment.
   *
   * <p>Validates input and delegates to DAO for insert operation.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param owner owner name/email
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> insertOwner(
      SqlConnection connection, UUID projectKey, UUID experimentId, String owner) {
    if (owner == null || owner.isEmpty()) {
      log.debug(
          "Service: No owner to insert for experimentId: {}, projectKey: {}",
          experimentId,
          projectKey);
      return Single.just(true);
    }

    log.info(
        "Service: Inserting owner: {} for experimentId: {}, projectKey: {}",
        owner,
        experimentId,
        projectKey);

    return ownerDAO
        .insertOwner(connection, projectKey, experimentId, owner)
        .doOnSuccess(
            success ->
                log.info(
                    "Service: Successfully inserted owner: {} for experimentId: {}, success: {}",
                    owner,
                    experimentId,
                    success))
        .doOnError(
            error ->
                log.error(
                    "Service: Failed to insert owner: {} for experimentId: {}, error: {}",
                    owner,
                    experimentId,
                    error.getMessage(),
                    error));
  }

  /**
   * Deletes all owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> deleteOwners(
      SqlConnection connection, UUID projectKey, UUID experimentId) {
    log.info(
        "Service: Deleting owners for experimentId: {}, projectKey: {}", experimentId, projectKey);

    return ownerDAO
        .deleteOwners(connection, projectKey, experimentId)
        .doOnSuccess(
            success ->
                log.info(
                    "Service: Successfully deleted owners for experimentId: {}, success: {}",
                    experimentId,
                    success))
        .doOnError(
            error ->
                log.error(
                    "Service: Failed to delete owners for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error));
  }
}
