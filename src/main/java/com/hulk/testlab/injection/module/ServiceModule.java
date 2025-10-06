package com.hulk.testlab.injection.module;

import com.hulk.testlab.client.aerospike.AerospikeClient;
import com.hulk.testlab.client.aerospike.impl.AerospikeClientImpl;
import com.hulk.testlab.client.datadog.DDClient;
import com.hulk.testlab.client.datadog.impl.DDClientImpl;
import com.hulk.testlab.client.kafka.KafkaProducerClient;
import com.hulk.testlab.client.kafka.impl.KafkaProducerClientImpl;
import com.hulk.testlab.client.mysql.MySQLReaderClient;
import com.hulk.testlab.client.mysql.MySQLWriterClient;
import com.hulk.testlab.client.mysql.impl.MySQLReaderClientImpl;
import com.hulk.testlab.client.mysql.impl.MySQLWriterClientImpl;
import com.hulk.testlab.client.webclient.WebClient;
import com.hulk.testlab.client.webclient.impl.WebClientImpl;
import com.hulk.testlab.config.*;
import com.hulk.testlab.dao.*;
import com.hulk.testlab.dao.impl.*;
import com.hulk.testlab.service.*;
import com.hulk.testlab.service.impl.*;
import com.hulk.testlab.util.CircuitBreakerFactory;
import com.google.inject.Singleton;
import io.vertx.rxjava3.core.Vertx;

public class ServiceModule extends DefaultModule {
  public ServiceModule(Vertx vertx) {
    super(vertx);
  }

  @Override
  protected void configure() {
    super.configure();
    /* Bind Configs */
    bindConfigs();
    /* Bind Clients */
    bindClients();
    /* Bind DAOs */
    bindDAOs();
    /* Bind Services */
    bindServices();
        /* Static Binding */
    requestStaticInjection(CircuitBreakerFactory.class);
      }

  private void bindConfigs() {
        bind(AerospikeConfig.class).toProvider(AerospikeConfig.provider()).asEagerSingleton();
        bind(ApplicationConfig.class).toProvider(ApplicationConfig.provider()).asEagerSingleton();
        bind(CircuitBreakerConfig.class).toProvider(CircuitBreakerConfig.provider()).asEagerSingleton();
        bind(HttpServerConfig.class).toProvider(HttpServerConfig.provider()).asEagerSingleton();
        bind(KafkaProducerConfig.class).toProvider(KafkaProducerConfig.provider()).asEagerSingleton();
            bind(MySQLConfig.class).toProvider(MySQLConfig.provider()).asEagerSingleton();
                    bind(WebClientConfig.class).toProvider(WebClientConfig.provider()).asEagerSingleton();
      }

  private void bindClients() {
        bind(AerospikeClientImpl.class).in(Singleton.class);
    bind(AerospikeClient.class).to(AerospikeClientImpl.class);
        bind(DDClientImpl.class).in(Singleton.class);
    bind(DDClient.class).to(DDClientImpl.class);
        bind(KafkaProducerClientImpl.class).in(Singleton.class);
    bind(KafkaProducerClient.class).to(KafkaProducerClientImpl.class);
            bind(MySQLReaderClientImpl.class).in(Singleton.class);
    bind(MySQLWriterClientImpl.class).in(Singleton.class);
    bind(MySQLReaderClient.class).to(MySQLReaderClientImpl.class);
    bind(MySQLWriterClient.class).to(MySQLWriterClientImpl.class);
                    bind(WebClientImpl.class).in(Singleton.class);
    bind(WebClient.class).to(WebClientImpl.class);
      }

  private void bindDAOs() {
    bind(HealthCheckDAO.class).to(HealthCheckDAOImpl.class);
  }

  private void bindServices() {
    bind(HealthCheckService.class).to(HealthCheckServiceImpl.class);
  }
}
