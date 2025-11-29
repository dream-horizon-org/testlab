package com.ascend.testlab.exception;

import com.dream11.rest.exception.RestError;
import com.dream11.rest.exception.RestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;

/**
 * Enum for the error codes and messages.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see RestError
 */
@Getter
@AllArgsConstructor
public enum ErrorEnum implements RestError {

  /* Server Errors */

  /** The error code for the health check failed. */
  REST_HEALTH_CHECK_FAILED(
      // TODO: Verify error code standards
      "testlab_REST_HEALTH_CHECK_FAILED",
      "HealthCheck Failed for testlab service",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for the tags listing failed. */
  REST_FETCH_TAGS_FAILED(
      "testlab_REST_FETCH_TAGS_FAILED",
      "Tags Listing failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  INVALID_REQUEST_BODY(
      "INVALID_REQUEST_BODY",
      "Request body param(s) is/are missing/invalid",
      HttpStatus.SC_BAD_REQUEST),

  /* Client Errors */
  /** The error code for experiment key availability check failed. */
  REST_EXPERIMENT_KEY_CHECK_FAILED(
      "testlab_REST_EXPERIMENT_KEY_CHECK_FAILED",
      "Failed to check experiment key availability",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for get experiment history failed. * */
  REST_FETCH_EXPERIMENT_HISTORY_FAILED(
      "testlab_REST_GET_EXPERIMENT_HISTORY_FAILED",
      "Failed to fetch experiment history due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for getting experiment by ID failed. */
  REST_GET_EXPERIMENT_BY_ID_FAILED(
      "testlab_REST_GET_EXPERIMENT_BY_ID_FAILED",
      "Get Experiment by Id failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for filtering experiments failed. */
  REST_FILTER_EXPERIMENTS_FAILED(
      "testlab_REST_FILTER_EXPERIMENTS_FAILED",
      "Filter Experiments failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for deleting experiment failed. */
  REST_DELETE_EXPERIMENT_FAILED(
      "testlab_REST_DELETE_EXPERIMENT_FAILED",
      "Delete experiment failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for failing reallocation. */
  REST_REALLOCATION_FAILED(
      "testlab_REST_REALLOCATION_FAILED",
      "Failed to reallocate user experiment",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  USER_COHORTS_SERVICE_REQUEST_FAILED(
      "USER_COHORTS_SERVICE_REQUEST_FAILED",
      "Cohorts service request failed with statusCode:%s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /* Client Errors */

  MISSING_USER_IDENTIFIER(
      "MISSING_USER_IDENTIFIER",
      "Missing User Identifier, pass user-id/guest-id",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when an invalid variant is present in the request payload. */
  INVALID_VARIANT_FOUND(
      "INVALID_VARIANT_FOUND", "Invalid variant found in request", HttpStatus.SC_BAD_REQUEST),

  /** The error code when the requested variant is already assigned to the user. */
  VARIANT_ALREADY_ASSIGNED(
      "VARIANT_ALREADY_ASSIGNED", "Given variant already assigned", HttpStatus.SC_BAD_REQUEST),

  /** The error code when no allotment is found. */
  NO_ALLOTMENT_FOUND(
      "NO_ALLOTMENT_FOUND",
      "No assignment found for given user, use overrides to allot variant",
      HttpStatus.SC_NOT_FOUND),

  /** The error code when experiment is not found. */
  EXPERIMENT_NOT_FOUND(
      "EXPERIMENT_NOT_FOUND",
      "Experiment not found for the given experimentId",
      HttpStatus.SC_NOT_FOUND),

  /** The error code when active experiment is not found. */
  ACTIVE_EXPERIMENT_NOT_FOUND(
      "ACTIVE_EXPERIMENT_NOT_FOUND",
      "Active Experiment not found for the given experimentId",
      HttpStatus.SC_NOT_FOUND),

  /** The error code for validating experiment status failed. */
  INVALID_EXPERIMENT_STATUS(
      "INVALID_EXPERIMENT_STATUS", "Experiment status is not valid", HttpStatus.SC_BAD_REQUEST),

  UPSTREAM_PARSING_ERROR(
      "UPSTREAM_PARSING_ERROR",
      "Upstream response json parsing error",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for validating experiment type failed. */
  INVALID_EXPERIMENT_TYPE(
      "INVALID_EXPERIMENT_TYPE", "Experiment type is not valid", HttpStatus.SC_BAD_REQUEST),

  /* Experiment Operation Errors */

  /** The error code for failed experiment creation. */
  EXPERIMENT_CREATION_FAILED(
      "EXPERIMENT_CREATION_FAILED",
      "Failed to create experiment. Please check your request and try again.",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for failed experiment update. */
  EXPERIMENT_UPDATE_FAILED(
      "EXPERIMENT_UPDATE_FAILED",
      "Failed to update experiment. Please check your request and try again.",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for failed enum validation. */
  ENUM_VALIDATION_FAILED(
      "ENUM_VALIDATION_FAILED", "Failed to validate enum value", HttpStatus.SC_BAD_REQUEST),

  /** The error code for failed row to experiment mapping. */
  ROW_MAPPING_FAILED(
      "ROW_MAPPING_FAILED",
      "Failed to map database row to experiment object",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code when variable data types are changed during update. */
  VARIABLE_DATA_TYPE_CHANGED(
      "VARIABLE_DATA_TYPE_CHANGED",
      "Variable data types cannot be changed during update",
      HttpStatus.SC_BAD_REQUEST),

  /* Update Validation Errors */

  /** The error code when trying to update an experiment in CONCLUDED or TERMINATED state. */
  UPDATE_NOT_ALLOWED_IN_TERMINAL_STATE(
      "UPDATE_NOT_ALLOWED_IN_TERMINAL_STATE",
      "Updates are not allowed for experiments in CONCLUDED or TERMINATED state",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to change cohort type (COHORT vs STRATIFIED). */
  COHORT_TYPE_CHANGE_NOT_ALLOWED(
      "COHORT_TYPE_CHANGE_NOT_ALLOWED",
      "Cohort type (COHORT/STRATIFIED) cannot be changed",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when stratified cohort is not in cohorts list. */
  STRATIFIED_COHORT_NOT_IN_COHORTS_LIST(
      "STRATIFIED_COHORT_NOT_IN_COHORTS_LIST",
      "Cohorts used in stratified variant weights must exist in the cohorts list",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to update experiment_key outside DRAFT mode. */
  EXPERIMENT_KEY_UPDATE_NOT_ALLOWED(
      "EXPERIMENT_KEY_UPDATE_NOT_ALLOWED",
      "Experiment key can only be updated in DRAFT mode",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to update hypothesis outside DRAFT mode. */
  HYPOTHESIS_UPDATE_NOT_ALLOWED(
      "HYPOTHESIS_UPDATE_NOT_ALLOWED",
      "Hypothesis can only be updated in DRAFT mode",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to update cohorts outside DRAFT mode. */
  COHORTS_UPDATE_NOT_ALLOWED(
      "COHORTS_UPDATE_NOT_ALLOWED",
      "Cohorts can only be updated in DRAFT mode",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to update startTime outside DRAFT mode. */
  START_TIME_UPDATE_NOT_ALLOWED(
      "START_TIME_UPDATE_NOT_ALLOWED",
      "Start time can only be updated in DRAFT mode",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when startTime is not in the future. */
  START_TIME_MUST_BE_FUTURE(
      "START_TIME_MUST_BE_FUTURE", "Start time must be in the future", HttpStatus.SC_BAD_REQUEST),

  /** The error code when endTime is not in the future. */
  END_TIME_MUST_BE_FUTURE(
      "END_TIME_MUST_BE_FUTURE", "End time must be in the future", HttpStatus.SC_BAD_REQUEST),

  /** The error code when variant keys don't match variant_weights keys. */
  VARIANT_WEIGHT_KEYS_MISMATCH(
      "VARIANT_WEIGHT_KEYS_MISMATCH",
      "Variant keys must match variant_weights keys",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when COHORT variant weights don't sum to 100. */
  VARIANT_WEIGHTS_SUM_NOT_100(
      "VARIANT_WEIGHTS_SUM_NOT_100",
      "Variant weights must sum to 100 for COHORT type",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when status transition is not allowed. */
  INVALID_STATUS_TRANSITION(
      "INVALID_STATUS_TRANSITION",
      "Invalid status transition. Allowed: DRAFT→LIVE, LIVE→PAUSED/CONCLUDED/TERMINATED, PAUSED→LIVE/TERMINATED",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when variant naming is invalid (missing control). */
  VARIANT_NAMING_INVALID(
      "VARIANT_NAMING_INVALID", "Variants must include 'control'", HttpStatus.SC_BAD_REQUEST),

  /** The error code when variant naming is not contiguous. */
  VARIANT_NAMING_NOT_CONTIGUOUS(
      "VARIANT_NAMING_NOT_CONTIGUOUS",
      "Variants must be named contiguously: control, variant1, variant2, etc.",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when variable keys are inconsistent across variants. */
  VARIABLE_KEYS_INCONSISTENT_ACROSS_VARIANTS(
      "VARIABLE_KEYS_INCONSISTENT_ACROSS_VARIANTS",
      "All variants must have the same variable keys",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to remove cohorts outside DRAFT mode. */
  COHORT_REMOVAL_NOT_ALLOWED(
      "COHORT_REMOVAL_NOT_ALLOWED",
      "Cohorts can only be removed in DRAFT mode",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when same variable key has different dataTypes across variants. */
  VARIABLE_DATA_TYPE_INCONSISTENT_ACROSS_VARIANTS(
      "VARIABLE_DATA_TYPE_INCONSISTENT_ACROSS_VARIANTS",
      "Same variable key must have the same dataType across all variants",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to remove a variant. */
  VARIANT_REMOVAL_NOT_ALLOWED(
      "VARIANT_REMOVAL_NOT_ALLOWED",
      "Variants cannot be removed. All existing variants must be included in the request",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to remove a variable key in LIVE/PAUSED mode. */
  VARIABLE_KEY_REMOVAL_NOT_ALLOWED(
      "VARIABLE_KEY_REMOVAL_NOT_ALLOWED",
      "Variable keys cannot be removed in LIVE or PAUSED mode. Only allowed in DRAFT mode",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to remove a variant from variant_weights. */
  VARIANT_WEIGHT_REMOVAL_NOT_ALLOWED(
      "VARIANT_WEIGHT_REMOVAL_NOT_ALLOWED",
      "Variants cannot be removed from variant_weights. All existing variants must be included",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to change experiment type. */
  TYPE_CHANGE_NOT_ALLOWED(
      "TYPE_CHANGE_NOT_ALLOWED",
      "Experiment type cannot be changed after creation",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to change assignment domain. */
  ASSIGNMENT_DOMAIN_CHANGE_NOT_ALLOWED(
      "ASSIGNMENT_DOMAIN_CHANGE_NOT_ALLOWED",
      "Assignment domain cannot be changed after creation",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when trying to change distribution strategy. */
  DISTRIBUTION_STRATEGY_CHANGE_NOT_ALLOWED(
      "DISTRIBUTION_STRATEGY_CHANGE_NOT_ALLOWED",
      "Distribution strategy cannot be changed after creation",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when rule condition dataType is changed. */
  RULE_CONDITION_DATA_TYPE_CHANGED(
      "RULE_CONDITION_DATA_TYPE_CHANGED",
      "Condition operandDataType cannot be changed for existing conditions",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when winning_variant is required but not provided. */
  WINNING_VARIANT_REQUIRED(
      "WINNING_VARIANT_REQUIRED",
      "winning_variant is required when concluding an experiment",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when winning_variant contains invalid variant keys. */
  WINNING_VARIANT_INVALID(
      "WINNING_VARIANT_INVALID",
      "winning_variant must contain valid variant keys from the experiment",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code when winning_variant is provided without concluding. */
  WINNING_VARIANT_ONLY_ON_CONCLUDE(
      "WINNING_VARIANT_ONLY_ON_CONCLUDE",
      "winning_variant can only be set when concluding the experiment",
      HttpStatus.SC_BAD_REQUEST);

  /** The error code. */
  private final String errorCode;

  /** The error message. */
  private final String errorMessage;

  /** The HTTP status code. */
  private final int httpStatusCode;

  /**
   * Handle the exception, mapping the throwable to a RestException.
   *
   * @param throwable the throwable
   * @param defaultException the default exception to return if the throwable is not a RestException
   * @return the rest exception
   */
  public static RestException handleException(Throwable throwable, RestException defaultException) {
    if (throwable instanceof RestException restException) return restException;
    else return defaultException;
  }
}
