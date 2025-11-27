package com.ascend.testlab.operation;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class for relational operations.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public final class RelationalOperation {

  /**
   * Check if op1 is greater than op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @param <T> the type of the operands
   * @return true if op1 is greater than op2, false otherwise
   */
  public static <T> boolean isGreaterThan(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) > 0;
  }

  /**
   * Check if op1 is greater than or equal to op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @param <T> the type of the operands
   * @return true if op1 is greater than or equal to op2, false otherwise
   */
  public static <T> boolean isGreaterThanOrEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) >= 0;
  }

  /**
   * Check if op1 is less than op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @param <T> the type of the operands
   * @return true if op1 is less than op2, false otherwise
   */
  public static <T> boolean isLessThan(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) < 0;
  }

  /**
   * Check if op1 is less than or equal to op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @param <T> the type of the operands
   * @return true if op1 is less than or equal to op2, false otherwise
   */
  public static <T> boolean isLessThanOrEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) <= 0;
  }

  /**
   * Check if op1 is equal to op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @param <T> the type of the operands
   * @return true if op1 is equal to op2, false otherwise
   */
  public static <T> boolean isEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) == 0;
  }

  /**
   * Check if op1 is equal to op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @return true if op1 is equal to op2, false otherwise
   */
  public static boolean isEqual(Object op1, Object op2) {
    return op1.equals(op2);
  }

  /**
   * Check if op1 is not equal to op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @param <T> the type of the operands
   * @return true if op1 is not equal to op2, false otherwise
   */
  public static <T> boolean isNotEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) != 0;
  }

  /**
   * Check if op1 is not equal to op2.
   *
   * @param op1 the first operand
   * @param op2 the second operand
   * @return true if op1 is not equal to op2, false otherwise
   */
  public static boolean isNotEqual(Object op1, Object op2) {
    return !op1.equals(op2);
  }

  /**
   * Check if op1 matches regex op2.
   *
   * @param op1 the first operand
   * @param op2 the regex pattern
   * @return true if op1 matches regex op2, false otherwise
   */
  public static boolean containsRegex(String op1, String op2) {
    return Pattern.compile(op2).matcher(op1).find();
  }

  /**
   * Check if op2 contains op1.
   *
   * @param op1 the element to check
   * @param op2 the collection
   * @param <T> the type of the elements
   * @return true if op2 contains op1, false otherwise
   */
  public static <T> boolean contains(T op1, Collection<T> op2) {
    return op2.contains(op1);
  }

  /**
   * Check if op2 contains op1.
   *
   * @param op1 the substring to check
   * @param op2 the string
   * @return true if op2 contains op1, false otherwise
   */
  public static boolean contains(String op1, String op2) {
    return op2.contains(op1);
  }

  /**
   * Check if any element in op1 is contained in op2.
   *
   * @param op1 the first collection
   * @param op2 the second collection
   * @param <T> the type of the elements
   * @return true if any element in op1 is contained in op2, false otherwise
   */
  public static <T> boolean contains(Collection<T> op1, Collection<T> op2) {
    return op1.stream().anyMatch(op2::contains);
  }

  /**
   * Check if op2 does not contain op1.
   *
   * @param op1 the element to check
   * @param op2 the collection
   * @param <T> the type of the elements
   * @return true if op2 does not contain op1, false otherwise
   */
  public static <T> boolean doesNotContain(T op1, Collection<T> op2) {
    return !op2.contains(op1);
  }

  /**
   * Check if no element in op1 is contained in op2.
   *
   * @param op1 the first collection
   * @param op2 the second collection
   * @param <T> the type of the elements
   * @return true if no element in op1 is contained in op2, false otherwise
   */
  public static <T> boolean doesNotContain(Collection<T> op1, Collection<T> op2) {
    return op2.stream().noneMatch(op1::contains);
  }

  /**
   * Check if op2 does not contain op1.
   *
   * @param op1 the substring to check
   * @param op2 the string
   * @return true if op2 does not contain op1, false otherwise
   */
  public static boolean doesNotContain(String op1, String op2) {
    return !op2.contains(op1);
  }

  /** Private constructor to prevent instantiation. */
  private RelationalOperation() {
    throw new UnsupportedOperationException("Constructor Invocation Unavailable");
  }
}
