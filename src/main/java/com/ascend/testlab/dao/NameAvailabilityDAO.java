package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object interface for checking if an experiment name is available.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public interface NameAvailabilityDAO {

  /**
   * Checks if an experiment name is available for a given project key.
   *
   * @param projectKey the project key
   * @param experimentName the experiment name
   * @return a Single that emits true if the experiment name is available, false otherwise
   */
  Single<Boolean> isExperimentNameAvailable(String projectKey, String experimentName);
}
