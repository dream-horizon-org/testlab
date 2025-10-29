package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object interface for experiment tags operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsDAOImpl
 */
public interface TagsDAO {

  /**
   * Fetches all distinct tags for experiments within the specified project.
   *
   * @param projectId the UUID of the project to fetch tags for
   * @return a Single containing a list of distinct tag strings
   * @throws com.dream11.rest.exception.RestException if the database operation fails
   */
  Single<List<String>> fetchTags(UUID projectId);
}
