package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.dto.response.GetCohortsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.CohortService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the CohortService interface. Fetches user cohorts from external cohort service
 * using WebClient with fallback to empty list if service is down.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see CohortService
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CohortServiceImpl implements CohortService {

  private final WebClient webClient;
  private final ApplicationConfig.ServiceConfig cohortsConfig;
  private final ObjectMapper objectMapper;

  @Override
  public Maybe<List<String>> getUserCohorts(String userId, UUID tenantId) {

    return Maybe.just(new ArrayList<>());
    //    if (userIdStringUtils.isBlank(()) {
    //      log.debug("No userId provided, returning empty cohorts");
    //      return Maybe.just(Collections.emptyList());
    //    }
    //
    //    log.debug("Fetching cohorts for user {} from cohort service", userId);
    //
    //    return Maybe.fromSingle(
    //        fetchCohortsFromService(userId, tenantId)
    //            .doOnSuccess(
    //                cohorts -> log.info("Fetched {} cohorts for user {}", cohorts.size(), userId))
    //            .doOnError(
    //                error ->
    //                    log.warn("Failed to fetch cohorts for user {}: {}", userId,
    // error.getMessage()))
    //            .onErrorReturnItem(Collections.emptyList()));
  }

  private Single<List<String>> fetchCohortsFromService(String userId, UUID tenantId) {

    Map<String, String> queryParams = new HashMap<>();
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("X-Tenant-Id", tenantId.toString());
    headers.add("X-user-Id", userId);
    headers.add("Content-Type", "application/json");

    return webClient
        .sendHTTPGETRequest(
            cohortsConfig,
            queryParams,
            headers,
            ErrorEnum.USER_COHORTS_SERVICE_REQUEST_FAILED,
            this::parseCohortResponse)
        .onErrorReturnItem(Collections.emptyList())
        .switchIfEmpty(Single.just(Collections.emptyList()));
  }

  private List<String> parseCohortResponse(JsonObject jsonObject) {
    try {
      if (Objects.isNull(jsonObject) || jsonObject.isEmpty()) {
        return Collections.emptyList();
      }

      GetCohortsResponse cohortResponse =
          objectMapper.readValue(jsonObject.encode(), GetCohortsResponse.class);

      if (Objects.nonNull(cohortResponse) && Objects.nonNull(cohortResponse.getCohorts())) {
        return cohortResponse.getCohorts();
      }

      return Collections.emptyList();
    } catch (Exception e) {
      log.error("Error parsing cohort response", e);
      return Collections.emptyList();
    }
  }
}
