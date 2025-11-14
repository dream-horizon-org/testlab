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
   * Fetches the history of an experiment.
   *
   * @param projectKey the project key
   * @param experimentId the experiment id
   * @return a Single containing a list of experiment history entries
   */
  Single<List<ExperimentHistoryEntry>> fetchExperimentHistory(
      String projectKey, String experimentId);
}
