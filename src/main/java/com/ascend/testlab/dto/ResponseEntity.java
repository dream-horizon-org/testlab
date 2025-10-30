package com.ascend.testlab.dto;

import lombok.Getter;

/**
 * Class encapsulating the response entity for the REST API.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public final class ResponseEntity {

  /**
   * Record encapsulating the success response for the REST API.
   *
   * @param <T> the type of the data
   */
  public record Success<T>(T data) {}

  /** Class encapsulating the failure response for the REST API. */
  @Getter
  public static class Failure {
    /** The error entity. */
    private final ErrorEntity error;

    /**
     * Constructor for the Failure class.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the error cause
     */
    public Failure(String code, String message, String cause) {
      this.error = new ErrorEntity(code, message, cause);
    }

    /**
     * Record encapsulating the error entity for the REST API.
     *
     * @param code the error code
     * @param message the error message
     * @param cause the error cause
     */
    public record ErrorEntity(String code, String message, String cause) {}
  }
}
