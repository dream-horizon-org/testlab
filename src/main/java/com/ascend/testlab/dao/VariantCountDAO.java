package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Data Access Object interface for variant count operations. Provides methods to retrieve variant
 * counts for experiments from Aerospike.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public interface VariantCountDAO {

  /**
   * Retrieves the variant count for a specific experiment from Aerospike.
   *
   * @param projectKey the project key associated with the experiment
   * @param experimentId the unique identifier of the experiment
   * @return a Single that emits a JsonObject containing variant counts, or an empty JsonObject if
   *     no variant count data is found
   */
  Single<JsonObject> getVariantCount(String projectKey, String experimentId);
}
