package com.hulk.testlab.dto;

import lombok.Getter;

public final class ResponseEntity {

  public record Success<T>(T data) {}

  @Getter
  public static class Failure {
    private final ErrorEntity error;

    public Failure(String code, String message, String cause) {
      this.error = new ErrorEntity(code, message, cause);
    }

    public record ErrorEntity(String code, String message, String cause) {}
  }
}
