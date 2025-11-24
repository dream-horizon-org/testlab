package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import com.typesafe.config.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the Aerospike client.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class AerospikeConfig {
  /** The default port for the Aerospike client. */
  private static final Integer DEFAULT_PORT = 3000;

  /** The host for the Aerospike client. */
  private String host;

  /** The port for the Aerospike client. */
  @Optional private Integer port = DEFAULT_PORT;

  /** The maximum number of retries for the Aerospike client. */
  private int maxRetries;

  /** The maximum number of connections per node for the Aerospike client. */
  private int maxConnsPerNode;

  /** The event loop size for the Aerospike client. */
  private int eventLoopSize;

  /**
   * The maximum number of commands in process for the Aerospike client.
   *
   * @see com.aerospike.client.async.EventPolicy#maxCommandsInProcess
   */
  private int maxCommandsInProcess;

  /**
   * The maximum number of commands in queue for the Aerospike client.
   *
   * @see com.aerospike.client.async.EventPolicy#maxCommandsInQueue
   */
  private int maxCommandsInQueue;

  /** The connection retry interval for the Aerospike client. */
  private long connectRetryIntervalMS;

  /** The namespace in which the data is stored. */
  private String namespace;

  /** The Aerospike set name for storing user allocations. */
  private String userAllocationsSet;

  /** The Aerospike set name for storing variant counts. */
  private String variantCountSet;

  /** The Aerospike set name for storing reallocation logs. */
  private String reallocationLogSet;

  /** The Aerospike set name for storing user locks. */
  private String userLockSet;

  /** The Aerospike bin name for storing variant count data. */
  private String variantCountBin;

  /** The Aerospike bin name for storing user lock data while allocation. */
  private String userAllocationLockBin;

  /** The Aerospike bin name for storing allocation map data. */
  private String allocationMapBin;

  /**
   * Get the provider for the Aerospike config.
   *
   * @return the provider for the Aerospike config
   * @see ConfigProvider
   */
  public static ConfigProvider<AerospikeConfig> provider() {
    return new ConfigProvider<>("aerospike", AerospikeConfig.class);
  }
}
