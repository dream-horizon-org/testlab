package com.hulk.testlab.util;

import io.vertx.core.Vertx;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class MaintenanceUtil {
  private static final String MAINTENANCE_FLAG = "__maintenance_flag";

  public static void setMaintenance(Vertx vertx) {
    log.info("Turning on maintenance mode");
    AtomicBoolean isUnderMaintenance = VertxUtil.getOrCreateSharedData(vertx, MAINTENANCE_FLAG, MaintenanceUtil::getAtomicBoolean);
    isUnderMaintenance.set(true);
  }

  public static AtomicBoolean isUnderMaintenance(Vertx vertx) {
    return VertxUtil.getOrCreateSharedData(vertx, MAINTENANCE_FLAG, MaintenanceUtil::getAtomicBoolean);
  }

  public static void clearMaintenance(Vertx vertx) {
    log.info("Turning off maintenance mode");
    AtomicBoolean isUnderMaintenance = VertxUtil.getOrCreateSharedData(vertx, MAINTENANCE_FLAG, MaintenanceUtil::getAtomicBoolean);
    isUnderMaintenance.set(false);
  }

  private static AtomicBoolean getAtomicBoolean() {
    AtomicBoolean atomicBoolean = new AtomicBoolean();
    atomicBoolean.set(false);
    return atomicBoolean;
  }
}
