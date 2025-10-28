package com.ascend.testlab.injection.module;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.aerospike.impl.AerospikeClientImpl;
import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.client.mysql.MySQLWriterClient;
import com.ascend.testlab.client.mysql.impl.MySQLReaderClientImpl;
import com.ascend.testlab.client.mysql.impl.MySQLWriterClientImpl;
import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.client.webclient.impl.WebClientImpl;
import com.ascend.testlab.config.*;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.dao.impl.HealthCheckDAOImpl;
import com.ascend.testlab.service.HealthCheckService;
import com.ascend.testlab.service.impl.HealthCheckServiceImpl;
import com.ascend.testlab.util.CircuitBreakerFactory;
import com.google.inject.Singleton;
import io.vertx.rxjava3.core.Vertx;

/**
 * Service module for the testlab application. Contains methods to bind the configs, clients, DAOs
 * and services.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see DefaultModule
 */
public class ServiceModule extends DefaultModule {

  /**
   * Constructor for the ServiceModule.
   *
   * @param vertx the Vertx instance
   */
  public ServiceModule(Vertx vertx) {
    super(vertx);
  }

  /** {@inheritDoc} */
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

  /** Bind the config classes as eager singletons. */
  private void bindConfigs() {
    bind(AerospikeConfig.class).toProvider(AerospikeConfig.provider()).asEagerSingleton();
    bind(ApplicationConfig.class).toProvider(ApplicationConfig.provider()).asEagerSingleton();
    bind(CircuitBreakerConfig.class).toProvider(CircuitBreakerConfig.provider()).asEagerSingleton();
    bind(HttpServerConfig.class).toProvider(HttpServerConfig.provider()).asEagerSingleton();
    bind(MySQLConfig.class).toProvider(MySQLConfig.provider()).asEagerSingleton();
    bind(WebClientConfig.class).toProvider(WebClientConfig.provider()).asEagerSingleton();
  }

  /** Bind the client interfaces to their implementations. */
  private void bindClients() {
    bind(AerospikeClientImpl.class).in(Singleton.class);
    bind(AerospikeClient.class).to(AerospikeClientImpl.class);
    bind(MySQLReaderClientImpl.class).in(Singleton.class);
    bind(MySQLWriterClientImpl.class).in(Singleton.class);
    bind(MySQLReaderClient.class).to(MySQLReaderClientImpl.class);
    bind(MySQLWriterClient.class).to(MySQLWriterClientImpl.class);
    bind(WebClientImpl.class).in(Singleton.class);
    bind(WebClient.class).to(WebClientImpl.class);
  }

  /** Bind the DAO interfaces to their implementations. */
  private void bindDAOs() {
    bind(HealthCheckDAO.class).to(HealthCheckDAOImpl.class);
  }

  /** Bind the service interfaces to their implementations. */
  private void bindServices() {
    bind(HealthCheckService.class).to(HealthCheckServiceImpl.class);
  }
}
