package com.ascend.testlab.service;

import io.reactivex.rxjava3.core.Maybe;
import java.util.List;
import java.util.UUID;

/**
 * Interface for the cohort service. Contains methods to fetch user cohorts from external cohort
 * service.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public interface CohortService {

  /**
   * Fetches cohorts for a given user from the external cohort service. Returns empty list if
   * service is down or user has no cohorts.
   *
   * @param userId user identifier
   * @param tenantId tenant identifier
   * @return Maybe containing list of cohort names, empty if service is down
   */
  Maybe<List<String>> getUserCohorts(String userId, UUID tenantId);
}
