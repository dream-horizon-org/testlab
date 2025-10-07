package com.ascend.testlab.util;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class VertxUtil {

  private static final String SHARED_DATA_MAP_NAME = "__vertx.sharedDataUtils";
  private static final String CLASS_PREFIX = "__class.";
  private static final String SHARED_DATA_DEFAULT_KEY = "__default.";

  public static <T> T getOrCreateSharedData(Vertx vertx, String name, Supplier<T> supplier) {
    LocalMap<String, ThreadSafe<T>> sharedDataMap =
        vertx.sharedData().getLocalMap(SHARED_DATA_MAP_NAME);
    return sharedDataMap.computeIfAbsent(name, k -> new ThreadSafe<>(supplier.get())).object();
  }

  public static <T> void setInstanceInSharedData(Vertx vertx, T instance) {
    setInstanceInSharedData(vertx, instance, SHARED_DATA_DEFAULT_KEY);
  }

  public static <T> void setInstanceInSharedData(Vertx vertx, T instance, String key) {
    getOrCreateSharedData(
        vertx, CLASS_PREFIX + instance.getClass().getName() + key, () -> instance);
  }

  public static <T> T getInstanceFromSharedData(Vertx vertx, Class<T> clazz) {
    return getInstanceFromSharedData(vertx, clazz, SHARED_DATA_DEFAULT_KEY);
  }

  public static <T> T getInstanceFromSharedData(Vertx vertx, Class<T> clazz, String key) {
    return getOrCreateSharedData(
        vertx,
        CLASS_PREFIX + clazz.getName() + key,
        () -> {
          throw new NoSuchElementException(
              String.format("Cannot find default instance of %s", clazz.getName()));
        });
  }

  public static <T> Single<T> singleFromCompletableFuture(CompletableFuture<T> completableFuture) {
    return Single.create(
        subscriber ->
            completableFuture.whenComplete(
                (result, error) -> {
                  if (Objects.isNull(error)) subscriber.onSuccess(result);
                  else subscriber.onError(error);
                }));
  }

  public static <T> CompletableFuture<T> completableFutureFromSingle(Single<T> single) {
    return single.toCompletionStage().toCompletableFuture();
  }

  record ThreadSafe<T>(T object) implements Shareable {}
}
