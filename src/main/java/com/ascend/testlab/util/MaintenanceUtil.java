package com.ascend.testlab.util;

import io.vertx.core.Vertx;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for maintenance mode.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@UtilityClass
public final class MaintenanceUtil {

  private static final String MAINTENANCE_FLAG = "__maintenance_flag";

  /**
   * Set the maintenance mode.
   *
   * @param vertx the Vertx instance
   */
  public static void setMaintenance(Vertx vertx) {
    log.info("Turning on maintenance mode");
    AtomicBoolean isUnderMaintenance =
        VertxUtil.getOrCreateSharedData(vertx, MAINTENANCE_FLAG, MaintenanceUtil::getAtomicBoolean);
    isUnderMaintenance.set(true);
  }

  /**
   * Check if the maintenance mode is enabled.
   *
   * @param vertx the Vertx instance
   * @return the maintenance mode
   */
  public static AtomicBoolean isUnderMaintenance(Vertx vertx) {
    return VertxUtil.getOrCreateSharedData(
        vertx, MAINTENANCE_FLAG, MaintenanceUtil::getAtomicBoolean);
  }

  /**
   * Clear the maintenance mode.
   *
   * @param vertx the Vertx instance
   */
  public static void clearMaintenance(Vertx vertx) {
    log.info("Turning off maintenance mode");
    AtomicBoolean isUnderMaintenance =
        VertxUtil.getOrCreateSharedData(vertx, MAINTENANCE_FLAG, MaintenanceUtil::getAtomicBoolean);
    isUnderMaintenance.set(false);
  }

  private static AtomicBoolean getAtomicBoolean() {
    return new AtomicBoolean(false);
  }
}
