package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Data Access Object interface for experiment tags operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public interface TagsDAO {

  /**
   * Fetches all distinct tags for experiments within the specified project.
   *
   * @param projectKey the project key to fetch tags for
   * @return a Single containing a list of distinct tag strings
   */
  Single<List<String>> fetchTags(String projectKey);
}
