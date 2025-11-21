package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
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
   * Checks if an experiment key is available for a given project key.
   *
   * @param projectKey the project key
   * @param experimentKey the experiment key
   * @return a Single that emits true if the experiment key is available, false otherwise
   */
  Single<Boolean> isExperimentKeyAvailable(String projectKey, String experimentKey);

  /**
   * Fetches the history of an experiment with pagination support.
   *
   * @param request the experiment history request containing project key, experiment id, limit, and
   *     page
   * @return a Single containing ExperimentHistoryResponse with history entries and pagination
   *     metadata
   */
  Single<ExperimentHistoryResponse> fetchExperimentHistory(ExperimentHistoryRequest request);
}
