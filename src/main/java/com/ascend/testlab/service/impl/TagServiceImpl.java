package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.TagDAO;
import com.ascend.testlab.service.TagService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of TagService for tag business logic.
 *
 * <p>This class handles tag operations with proper validation, error handling, and logging.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class TagServiceImpl implements TagService {

  private final TagDAO tagDAO;

  /**
   * Constructs TagServiceImpl with tag DAO.
   *
   * @param tagDAO tag data access object
   */
  @Inject
  public TagServiceImpl(TagDAO tagDAO) {
    this.tagDAO = tagDAO;
  }

  /**
   * Batch inserts tags for an experiment.
   *
   * <p>Validates input and delegates to DAO for batch insert operation.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to insert
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> insertTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      log.debug(
          "Service: No tags to insert for experimentId: {}, projectKey: {}",
          experimentId,
          projectKey);
      return Single.just(true);
    }

    log.info(
        "Service: Inserting {} tags for experimentId: {}, projectKey: {}",
        tags.size(),
        experimentId,
        projectKey);

    return tagDAO
        .batchInsertTags(connection, projectKey, experimentId, tags)
        .doOnSuccess(
            success ->
                log.info(
                    "Service: Successfully inserted {} tags for experimentId: {}, success: {}",
                    tags.size(),
                    experimentId,
                    success))
        .doOnError(
            error ->
                log.error(
                    "Service: Failed to insert tags for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error));
  }

  /**
   * Updates tags for an experiment by merging with existing tags.
   *
   * <p>Gets existing active tags, marks removed tags as inactive (status=0), and inserts new tags
   * with status=1.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param newTags list of new tags to set
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> updateTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> newTags) {
    if (newTags == null || newTags.isEmpty()) {
      log.debug(
          "Service: No new tags provided for experimentId: {}, projectKey: {}",
          experimentId,
          projectKey);
      return Single.just(true);
    }

    log.info(
        "Service: Updating tags for experimentId: {}, projectKey: {}, newTags count: {}",
        experimentId,
        projectKey,
        newTags.size());

    // 1. Get existing active tags
    return tagDAO
        .getActiveTags(connection, projectKey, experimentId)
        .flatMap(
            existingTags -> {
              log.debug(
                  "Service: Existing active tags count: {} for experimentId: {}",
                  existingTags.size(),
                  experimentId);

              // 2. Find tags to remove (in existing but not in new)
              List<String> tagsToRemove =
                  existingTags.stream()
                      .filter(tag -> !newTags.contains(tag))
                      .collect(java.util.stream.Collectors.toList());

              // 3. Find tags to add (in new but not in existing)
              List<String> tagsToAdd =
                  newTags.stream()
                      .filter(tag -> !existingTags.contains(tag))
                      .collect(java.util.stream.Collectors.toList());

              log.info(
                  "Service: Tags to remove: {}, Tags to add: {} for experimentId: {}",
                  tagsToRemove.size(),
                  tagsToAdd.size(),
                  experimentId);

              // 4. Mark removed tags as inactive
              Single<Boolean> markInactive =
                  tagsToRemove.isEmpty()
                      ? Single.just(true)
                      : tagDAO.markTagsInactive(connection, projectKey, experimentId, tagsToRemove);

              // 5. Insert new tags (UPSERT will reactivate if they exist)
              Single<Boolean> insertNew =
                  tagsToAdd.isEmpty()
                      ? Single.just(true)
                      : tagDAO.batchInsertTags(connection, projectKey, experimentId, tagsToAdd);

              // 6. Execute both operations sequentially
              return markInactive.flatMap(
                  markSuccess -> {
                    if (!markSuccess) {
                      return Single.error(new RuntimeException("Failed to mark tags as inactive"));
                    }
                    return insertNew;
                  });
            })
        .doOnSuccess(
            success ->
                log.info(
                    "Service: Successfully updated tags for experimentId: {}, success: {}",
                    experimentId,
                    success))
        .doOnError(
            error ->
                log.error(
                    "Service: Failed to update tags for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error));
  }

  /**
   * Deletes all tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> deleteTags(SqlConnection connection, UUID projectKey, UUID experimentId) {
    log.info(
        "Service: Deleting tags for experimentId: {}, projectKey: {}", experimentId, projectKey);

    return tagDAO
        .deleteTags(connection, projectKey, experimentId)
        .doOnSuccess(
            success ->
                log.info(
                    "Service: Successfully deleted tags for experimentId: {}, success: {}",
                    experimentId,
                    success))
        .doOnError(
            error ->
                log.error(
                    "Service: Failed to delete tags for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error));
  }
}
