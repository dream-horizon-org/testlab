package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KafkaProducerConfig {
  private String bootstrapServer;
  private String keySerializerClass;
  private String valueSerializerClass;
  private String acks;
  private String topic;

  public static ConfigProvider<KafkaProducerConfig> provider() {
    return new ConfigProvider<>("kafka-producer", KafkaProducerConfig.class);
  }
}
