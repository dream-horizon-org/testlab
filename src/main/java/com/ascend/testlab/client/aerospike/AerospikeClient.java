package com.ascend.testlab.client.aerospike;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Interface for the client to interact with Aerospike database.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface AerospikeClient {

  /**
   * Close the client and release resources.
   *
   * @return a Completable that completes when the client is closed
   */
  Completable close();

  /**
   * Check if the client is connected to Aerospike database.
   *
   * @return a Single that emits true if the client is connected, false otherwise
   */
  Single<Boolean> isConnected();

  /**
   * Get the default policy for the client.
   *
   * @return the default policy
   */
  Policy getDefaultPolicy();

  /**
   * Get the default write policy for the client.
   *
   * @return the default write policy
   */
  WritePolicy getDefaultWritePolicy();

  /**
   * Get a record from Aerospike database.
   *
   * @param policy the policy to use for the operation
   * @param key the key of the record
   * @param binNames the names of the bins to get
   * @return a Single that emits the record
   */
  // TODO: add support for get method without policy and key specification
  Single<Record> get(Policy policy, Key key, String... binNames);

  /**
   * Get a list of records from Aerospike database.
   *
   * @param batchPolicy the policy to use for the operation
   * @param keys the keys of the records
   * @param binNames the names of the bins to get
   * @return a Single that emits the record
   */
  Single<List<Record>> get(BatchPolicy batchPolicy, List<Key> keys, String... binNames);

  /**
   * Put a record into Aerospike database.
   *
   * @param writePolicy the write policy to use for the operation
   * @param key the key of the record
   * @param bins the bins to put
   * @return a Single that emits the key of the record
   */
  Single<Key> put(WritePolicy writePolicy, Key key, Bin... bins);

  /**
   * Operate on a record in Aerospike database.
   *
   * @param writePolicy the write policy to use for the operation
   * @param key the key of the record
   * @param operations the operations to perform
   * @return a Single that emits the record
   */
  Single<Record> operate(WritePolicy writePolicy, Key key, Operation... operations);

  /**
   * Delete a record from Aerospike database.
   *
   * @param writePolicy the write policy to use for the operation
   * @param key the key of the record
   * @return a Single that emits true if the record is deleted, false otherwise
   */
  Single<Boolean> delete(WritePolicy writePolicy, Key key);
}
