package com.ascend.testlab.injection;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.List;
import java.util.Objects;
import lombok.Synchronized;

/**
 * Guice injector for the testlab application. Contains methods to initialize the injector and get
 * the instance of the injector.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public final class GuiceInjector {

  /** The instance of the GuiceInjector. */
  private static GuiceInjector instance = null;

  /** The injector instance. */
  private final Injector injector;

  /**
   * Constructor for the GuiceInjector.
   *
   * @param modules the modules to be injected
   */
  private GuiceInjector(List<Module> modules) {
    this.injector = Guice.createInjector(modules);
  }

  /**
   * Initialize the injector.
   *
   * @param modules the modules to be injected
   * @throws IllegalStateException if the injector is already initialized
   * @see Injector
   * @see Module
   */
  @Synchronized
  public static void initializeInjector(List<Module> modules) {
    if (Objects.nonNull(instance)) {
      throw new IllegalStateException("GuiceInjector is already initialized");
    } else {
      instance = new GuiceInjector(modules);
    }
  }

  /**
   * Get the instance of the GuiceInjector.
   *
   * @return the instance of the GuiceInjector
   * @throws NullPointerException if the instance is null
   */
  private static GuiceInjector instance() {
    return Objects.requireNonNull(instance);
  }

  /**
   * Get the instance of the specified class.
   *
   * @param clazz the class to get the instance of
   * @param <T> the type of the class
   * @return the instance of the class
   * @throws NullPointerException if the instance is null
   */
  public static <T> T getInstance(Class<T> clazz) {
    return instance().injector.getInstance(clazz);
  }
}
