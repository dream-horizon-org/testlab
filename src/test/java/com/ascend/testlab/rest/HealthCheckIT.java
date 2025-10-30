package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(Setup.class)
class HealthCheckIT {

  private final String route = "/healthcheck";

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", HealthCheckIT.class.getSimpleName());
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", HealthCheckIT.class.getSimpleName());
  }

  @Test
  void testHealthCheck() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(this.route));
    response.statusCode(HttpStatus.SC_OK);
  }
}
