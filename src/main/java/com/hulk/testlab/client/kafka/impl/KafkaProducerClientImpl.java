package com.hulk.testlab.client.kafka.impl;

import com.hulk.testlab.client.datadog.DDClient;
import com.hulk.testlab.client.kafka.KafkaProducerClient;
import com.hulk.testlab.config.KafkaProducerConfig;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.kafka.client.producer.KafkaProducer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;

@Slf4j
public class KafkaProducerClientImpl implements KafkaProducerClient {

  private final DDClient ddClient;
  private final KafkaProducer<String, String> kafkaProducer;
  private final KafkaProducerConfig producerConfig;

  @Inject
  public KafkaProducerClientImpl(
      Vertx vertx, KafkaProducerConfig producerConfig, DDClient ddClient) {
    this.ddClient = ddClient;
    this.producerConfig = producerConfig;
    this.kafkaProducer = KafkaProducer.create(vertx, getConfigMap(producerConfig));
  }

  @Override
  public Completable close() {
    return kafkaProducer.rxClose();
  }

  private static Map<String, String> getConfigMap(KafkaProducerConfig producerConfig) {
    Map<String, String> configMap = new HashMap<>();
    configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerConfig.getBootstrapServer());
    configMap.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerConfig.getKeySerializerClass());
    configMap.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerConfig.getValueSerializerClass());
    configMap.put(ProducerConfig.ACKS_CONFIG, producerConfig.getAcks());
    return configMap;
  }
}
