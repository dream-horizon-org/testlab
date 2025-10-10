package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/v1")
public class CreateExperiment {
  @Inject private ExperimentService experimentService;

  @PUT
  @Path("/experiment")
  public CompletionStage<ResponseEntity.Success<String>> approveWinnerDeclaration(
      CreateExperimentRequest winnerApproval) {
    return CompletableFuture.supplyAsync(() -> new ResponseEntity.Success<>("SUCCESS"));
  }
}
