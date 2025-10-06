package com.hulk.testlab.rest;

import com.hulk.testlab.dto.response.HealthCheckResponse;
import com.hulk.testlab.service.HealthCheckService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;

@Path("/healthcheck")
@Hidden
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class HealthCheck {

  private final HealthCheckService healthCheckService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<HealthCheckResponse> healthCheckHandle() {
    return healthCheckService.healthCheck().toCompletionStage();
  }
}
