package com.ascend.testlab.util;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Map;
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

  /** PostgreSQL error code for unique constraint violations. */
  private static final String UNIQUE_VIOLATION_CODE = "23505";

  /** Constraint name to user-friendly message mapping. */
  private static final Map<String, String> CONSTRAINT_MESSAGES =
      Map.of(
          "experiment_key_unique_check",
              "An experiment with this experiment_key already exists in the project",
          "experiment_key_key",
              "An experiment with this experiment_key already exists in the project",
          "name_unique_check", "An experiment with this name already exists in the project");

  private static final String DEFAULT_DUPLICATE_MESSAGE = "Duplicate entry detected";

  /**
   * Handles database errors and converts them to RestException.
   *
   * @param err the error
   * @param defaultError the default error enum to use if not a constraint violation
   * @return RestException
   */
  public static RestException handleDbError(Throwable err, ErrorEnum defaultError) {
    String duplicateMessage = findUniqueViolationMessage(err);
    if (duplicateMessage != null) {
      return new RestException(
          "DUPLICATE_EXPERIMENT", duplicateMessage, HttpStatus.SC_BAD_REQUEST, err);
    }
    return ErrorEnum.handleException(err, new RestException(defaultError, err));
  }

  /**
   * Walks the exception cause chain to find a unique constraint violation. Returns user-friendly
   * message if found, null otherwise.
   */
  private static String findUniqueViolationMessage(Throwable err) {
    for (Throwable current = err; current != null; current = current.getCause()) {
      String message = current.getMessage();
      if (message == null) {
        continue;
      }

      // Check for PostgreSQL unique violation code
      if (!message.contains(UNIQUE_VIOLATION_CODE)
          && !message.contains("duplicate key value violates unique constraint")) {
        continue;
      }

      // Found a unique violation - extract friendly message from constraint name
      for (Map.Entry<String, String> entry : CONSTRAINT_MESSAGES.entrySet()) {
        if (message.contains(entry.getKey())) {
          return entry.getValue();
        }
      }
      return DEFAULT_DUPLICATE_MESSAGE;
    }
    return null;
  }
}
