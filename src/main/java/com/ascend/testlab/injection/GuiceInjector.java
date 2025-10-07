package com.ascend.testlab.injection;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.List;
import java.util.Objects;
import lombok.Synchronized;

public final class GuiceInjector {

  private static GuiceInjector instance = null;
  private final Injector injector;

  private GuiceInjector(List<Module> modules) {
    this.injector = Guice.createInjector(modules);
  }

  @Synchronized
  public static void initializeInjector(List<Module> modules) {
    if (Objects.nonNull(instance)) {
      throw new IllegalStateException("GuiceInjector is already initialized");
    } else {
      instance = new GuiceInjector(modules);
    }
  }

  private static GuiceInjector instance() {
    return Objects.requireNonNull(instance);
  }

  public static <T> T getInstance(Class<T> clazz) {
    return instance().injector.getInstance(clazz);
  }
}
