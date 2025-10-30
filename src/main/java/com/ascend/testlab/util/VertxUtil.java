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

/**
 * Utility class for Vertx related operations.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class VertxUtil {

  /** The name of the shared data map. */
  private static final String SHARED_DATA_MAP_NAME = "__vertx.sharedDataUtils";

  /** The prefix for the class name. */
  private static final String CLASS_PREFIX = "__class.";

  /** The default key for the shared data. */
  private static final String SHARED_DATA_DEFAULT_KEY = "__default.";

  /**
   * Get the shared data from the shared data map. If not found, create a new one.
   *
   * @param vertx the Vertx instance
   * @param name the name of the shared data
   * @param supplier the supplier of the shared data
   * @param <T> the type of the shared data
   * @return the shared data
   */
  public static <T> T getOrCreateSharedData(Vertx vertx, String name, Supplier<T> supplier) {
    LocalMap<String, ThreadSafe<T>> sharedDataMap =
        vertx.sharedData().getLocalMap(SHARED_DATA_MAP_NAME);
    return sharedDataMap.computeIfAbsent(name, k -> new ThreadSafe<>(supplier.get())).object();
  }

  /**
   * Set the instance in the shared data map.
   *
   * @param vertx the Vertx instance
   * @param instance the instance to set
   * @param <T> the type of the instance
   */
  public static <T> void setInstanceInSharedData(Vertx vertx, T instance) {
    setInstanceInSharedData(vertx, instance, SHARED_DATA_DEFAULT_KEY);
  }

  /**
   * Set the instance in the shared data map.
   *
   * @param vertx the Vertx instance
   * @param instance the instance to set
   * @param key the key of the shared data
   * @param <T> the type of the instance
   */
  public static <T> void setInstanceInSharedData(Vertx vertx, T instance, String key) {
    getOrCreateSharedData(
        vertx, CLASS_PREFIX + instance.getClass().getName() + key, () -> instance);
  }

  /**
   * Get the instance from the shared data map.
   *
   * @param vertx the Vertx instance
   * @param clazz the class of the instance
   * @param <T> the type of the instance
   * @return the instance
   */
  public static <T> T getInstanceFromSharedData(Vertx vertx, Class<T> clazz) {
    return getInstanceFromSharedData(vertx, clazz, SHARED_DATA_DEFAULT_KEY);
  }

  /**
   * Get the instance from the shared data map.
   *
   * @param vertx the Vertx instance
   * @param clazz the class of the instance
   * @param key the key of the shared data
   * @param <T> the type of the instance
   * @return the instance
   */
  public static <T> T getInstanceFromSharedData(Vertx vertx, Class<T> clazz, String key) {
    return getOrCreateSharedData(
        vertx,
        CLASS_PREFIX + clazz.getName() + key,
        () -> {
          throw new NoSuchElementException(
              String.format("Cannot find default instance of %s", clazz.getName()));
        });
  }

  /**
   * Convert a CompletableFuture to a Single.
   *
   * @param completableFuture the CompletableFuture to convert
   * @param <T> the type of the result
   * @return the Single that emits the result of the CompletableFuture
   */
  public static <T> Single<T> singleFromCompletableFuture(CompletableFuture<T> completableFuture) {
    return Single.create(
        subscriber ->
            completableFuture.whenComplete(
                (result, error) -> {
                  if (Objects.isNull(error)) subscriber.onSuccess(result);
                  else subscriber.onError(error);
                }));
  }

  /**
   * Convert a Single to a CompletableFuture.
   *
   * @param single the Single to convert
   * @param <T> the type of the result
   * @return the CompletableFuture that emits the result of the Single
   */
  public static <T> CompletableFuture<T> completableFutureFromSingle(Single<T> single) {
    return single.toCompletionStage().toCompletableFuture();
  }

  /**
   * Record for a thread safe object.
   *
   * @param <T> the type of the object
   */
  record ThreadSafe<T>(T object) implements Shareable {}
}
