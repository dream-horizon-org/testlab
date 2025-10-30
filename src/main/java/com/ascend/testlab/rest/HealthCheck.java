package com.ascend.testlab.rest;

import com.ascend.testlab.dto.response.HealthCheckResponse;
import com.ascend.testlab.service.HealthCheckService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

/**
 * Health check endpoint for the testlab application. Contains methods to handle the health check
 * request.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see HealthCheckService
 */
@Path("/healthcheck")
@Hidden
public class HealthCheck {

  /** The health check service. */
  private final HealthCheckService healthCheckService;

  /**
   * Constructor for the HealthCheck.
   *
   * @param healthCheckService the health check service
   */
  @Inject
  public HealthCheck(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  /**
   * Handle the health check request.
   *
   * @return the health check response
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<HealthCheckResponse> healthCheckHandle() {
    return healthCheckService.healthCheck().toCompletionStage();
  }
}
