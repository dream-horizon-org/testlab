package com.hulk.testlab.client.aerospike;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface AerospikeClient {
  Completable close();

  Single<Boolean> isConnected();

  Single<Record> get(Policy policy, Key key, String... binNames);

  Single<Key> put(WritePolicy writePolicy, Key key, Bin... bins);

  Single<Record> operate(WritePolicy writePolicy, Key key, Operation... operations);

  Single<Boolean> delete(WritePolicy writePolicy, Key key);
}
