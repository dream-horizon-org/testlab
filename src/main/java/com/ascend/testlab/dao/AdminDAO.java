package com.ascend.testlab.dao;

import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Data Access Object interface for admin operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public interface AdminDAO {

  /**
   * Fetches all distinct tags for experiments within the specified project.
   *
   * @param projectKey the project key to fetch tags for
   * @return a Single containing a list of distinct tag strings
   */
  Single<List<String>> fetchTags(String projectKey);

  /**
   * Checks if an experiment name is available for a given project key.
   *
   * @param projectKey the project key
   * @param experimentName the experiment name
   * @return a Single that emits true if the experiment name is available, false otherwise
   */
  Single<Boolean> isExperimentNameAvailable(String projectKey, String experimentName);

  /**
   * Fetches the history of an experiment with pagination support.
   *
   * @param projectKey the project key
   * @param experimentId the experiment id
   * @param limit the maximum number of history entries to return
   * @param offset the number of history entries to skip
   * @return a Single containing ExperimentHistoryResult with history entries and total count
   */
  Single<ExperimentHistoryResult> fetchExperimentHistory(
      String projectKey, String experimentId, int limit, int offset);

  /**
   * Result record containing history entries and total count for pagination.
   *
   * @param historyEntries the list of experiment history entries for the current page
   * @param totalCount the total number of history entries matching the filter
   */
  record ExperimentHistoryResult(List<ExperimentHistoryEntry> historyEntries, int totalCount) {}
}
