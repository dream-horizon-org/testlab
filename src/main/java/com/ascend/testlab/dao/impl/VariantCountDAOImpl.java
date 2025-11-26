package com.ascend.testlab.dao.impl;

import com.aerospike.client.Key;
import com.aerospike.client.policy.Policy;
import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.config.AerospikeConfig;
import com.ascend.testlab.dao.VariantCountDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of the VariantCountDAO interface for retrieving variant counts from Aerospike.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see VariantCountDAO
 * @see AerospikeClient
 */
public class VariantCountDAOImpl implements VariantCountDAO {

  private final AerospikeClient aerospikeClient;
  private final AerospikeConfig aerospikeConfig;

  /**
   * Constructs a new VariantCountDAOImpl.
   *
   * @param aerospikeClient the Aerospike client for database operations
   * @param aerospikeConfig the Aerospike configuration containing namespace, set, and bin names
   */
  @Inject
  public VariantCountDAOImpl(AerospikeClient aerospikeClient, AerospikeConfig aerospikeConfig) {
    this.aerospikeClient = aerospikeClient;
    this.aerospikeConfig = aerospikeConfig;
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public Single<JsonObject> getVariantCount(String projectKey, String experimentId) {
    Key key =
        new Key(aerospikeConfig.getNamespace(), aerospikeConfig.getVariantCountSet(), experimentId);

    Policy policy = aerospikeClient.getDefaultPolicy();

    return aerospikeClient
        .get(policy, key, aerospikeConfig.getVariantCountBin())
        .map(
            record -> {
              if (record == null) {
                return new JsonObject();
              }
              Map<String, Object> variantCountMap =
                  (Map<String, Object>) record.getMap(aerospikeConfig.getVariantCountBin());
              if (Objects.isNull(variantCountMap) || variantCountMap.isEmpty()) {
                return new JsonObject();
              }

              JsonObject jsonObject = new JsonObject();
              variantCountMap.forEach((k, v) -> jsonObject.put(String.valueOf(k), v));
              return jsonObject;
            });
  }
}
