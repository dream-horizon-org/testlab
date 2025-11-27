package com.ascend.testlab.operation;

import java.util.Collection;
import java.util.regex.Pattern;

public final class RelationalOperation {

  public static <T> boolean isGreaterThan(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) > 0;
  }

  public static <T> boolean isGreaterThanOrEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) >= 0;
  }

  public static <T> boolean isLessThan(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) < 0;
  }

  public static <T> boolean isLessThanOrEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) <= 0;
  }

  public static <T> boolean isEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) == 0;
  }

  public static boolean isEqual(Object op1, Object op2) {
    return op1.equals(op2);
  }

  public static <T> boolean isNotEqual(Comparable<T> op1, T op2) {
    return op1.compareTo(op2) != 0;
  }

  public static boolean isNotEqual(Object op1, Object op2) {
    return !op1.equals(op2);
  }

  public static boolean containsRegex(String op1, String op2) {
    return Pattern.compile(op2).matcher(op1).find();
  }

  public static <T> boolean contains(T op1, Collection<T> op2) {
    return op2.contains(op1);
  }

  public static boolean contains(String op1, String op2) {
    return op2.contains(op1);
  }

  public static <T> boolean contains(Collection<T> op1, Collection<T> op2) {
    return op1.stream().anyMatch(op2::contains);
  }

  public static <T> boolean doesNotContain(T op1, Collection<T> op2) {
    return !op2.contains(op1);
  }

  public static <T> boolean doesNotContain(Collection<T> op1, Collection<T> op2) {
    return op2.stream().noneMatch(op1::contains);
  }

  public static boolean doesNotContain(String op1, String op2) {
    return !op2.contains(op1);
  }

  private RelationalOperation() {
    throw new UnsupportedOperationException("Constructor Invocation Unavailable");
  }
}
