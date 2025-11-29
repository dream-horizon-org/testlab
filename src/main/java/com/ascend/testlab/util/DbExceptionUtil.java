package com.ascend.testlab.util;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpStatus;

/**
 * Utility class for handling database exceptions.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public class DbExceptionUtil {

  private static final String UNIQUE_VIOLATION_CODE = "23505";
  private static final String TRANSACTION_ABORTED_CODE = "25P02";

  /** Constraint patterns mapped to user-friendly messages. */
  private static final Map<String, String> CONSTRAINT_MESSAGES =
      Map.of(
          "experiment_key_unique_check",
              "An experiment with this experiment_key already exists in the project",
          "experiment_key_key",
              "An experiment with this experiment_key already exists in the project",
          "name_unique_check", "An experiment with this name already exists in the project");

  /**
   * Handles database errors and converts them to RestException.
   *
   * @param err the error
   * @param defaultError the default error enum to use if not a constraint violation
   * @return RestException
   */
  public static RestException handleDbError(Throwable err, ErrorEnum defaultError) {
    if (isUniqueConstraintViolation(err)) {
      return createDuplicateException(err);
    }
    return ErrorEnum.handleException(err, new RestException(defaultError, err));
  }

  private static boolean isUniqueConstraintViolation(Throwable err) {
    return findInChain(err, DbExceptionUtil::isUniqueViolation);
  }

  private static boolean isUniqueViolation(Throwable t) {
    String message = t.getMessage();
    if (message == null) return false;

    return message.contains(UNIQUE_VIOLATION_CODE)
        || message.contains(TRANSACTION_ABORTED_CODE)
        || message.contains("duplicate key value violates unique constraint")
        || CONSTRAINT_MESSAGES.keySet().stream().anyMatch(message::contains);
  }

  private static RestException createDuplicateException(Throwable err) {
    String message = extractConstraintMessage(err).orElse("Duplicate entry detected");
    return new RestException("DUPLICATE_EXPERIMENT", message, HttpStatus.SC_BAD_REQUEST, err);
  }

  private static Optional<String> extractConstraintMessage(Throwable err) {
    Throwable current = err;
    while (current != null) {
      String message = current.getMessage();
      if (message != null) {
        for (Map.Entry<String, String> entry : CONSTRAINT_MESSAGES.entrySet()) {
          if (message.contains(entry.getKey())) {
            return Optional.of(entry.getValue());
          }
        }
      }
      current = current.getCause();
    }
    return Optional.empty();
  }

  private static boolean findInChain(Throwable err, java.util.function.Predicate<Throwable> check) {
    Throwable current = err;
    while (current != null) {
      if (check.test(current)) return true;
      for (Throwable suppressed : current.getSuppressed()) {
        if (findInChain(suppressed, check)) return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
