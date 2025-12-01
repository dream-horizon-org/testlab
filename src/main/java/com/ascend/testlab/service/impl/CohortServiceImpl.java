package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.dto.response.GetCohortsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.CohortService;
import com.ascend.testlab.util.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.MultiMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of the CohortService interface. Fetches user cohorts from external cohort service
 * using WebClient with fallback to empty list if service is down.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see CohortService
 */
@Slf4j
public class CohortServiceImpl implements CohortService {

  private final WebClient webClient;
  private final ApplicationConfig.ServiceConfig cohortsConfig;

  /**
   * Constructor for CohortServiceImpl.
   *
   * @param webClient the web client
   * @param applicationConfig the application configuration
   * @param objectMapper the object mapper
   */
  @Inject
  public CohortServiceImpl(
      WebClient webClient, ApplicationConfig applicationConfig, ObjectMapper objectMapper) {
    this.webClient = webClient;
    this.cohortsConfig = applicationConfig.getCohortsConfig();
  }

  @Override
  public Maybe<List<String>> getUserCohorts(String userId, String projectKey) {

    if (StringUtils.isBlank(userId)) {
      log.debug("No userId provided, returning empty cohorts");
      return Maybe.just(Collections.emptyList());
    }

    log.debug("Fetching cohorts for user {} from cohort service", userId);

    return Maybe.fromSingle(
        fetchCohortsFromService(userId, projectKey)
            .map(GetCohortsResponse::getCohorts)
            .doOnSuccess(
                cohorts -> log.info("Fetched {} cohorts for user {}", cohorts.size(), userId))
            .doOnError(
                error ->
                    log.warn("Failed to fetch cohorts for user {}: {}", userId, error.getMessage()))
            .onErrorReturnItem(Collections.emptyList()));
  }

  private Single<GetCohortsResponse> fetchCohortsFromService(String userId, String projectKey) {

    Map<String, String> queryParams = new HashMap<>();
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("X-Tenant-Id", projectKey);
    headers.add("X-user-Id", userId);
    headers.add("Content-Type", "application/json");

    return webClient.sendHTTPGETRequest(
        cohortsConfig,
        queryParams,
        headers,
        ErrorEnum.USER_COHORTS_SERVICE_REQUEST_FAILED,
        CommonUtil.mapResponse(GetCohortsResponse.class));
  }
}
