package com.ascend.testlab.injection.module;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.aerospike.impl.AerospikeClientImpl;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.client.postgresql.impl.PgReaderClientImpl;
import com.ascend.testlab.client.postgresql.impl.PgWriterClientImpl;
import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.client.webclient.impl.WebClientImpl;
import com.ascend.testlab.config.*;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.dao.TagsDAO;
import com.ascend.testlab.dao.impl.HealthCheckDAOImpl;
import com.ascend.testlab.dao.impl.TagsDAOImpl;
import com.ascend.testlab.service.HealthCheckService;
import com.ascend.testlab.service.TagsService;
import com.ascend.testlab.service.impl.HealthCheckServiceImpl;
import com.ascend.testlab.service.impl.TagsServiceImpl;
import com.ascend.testlab.util.CircuitBreakerFactory;
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
    bind(PostgreSQLConfig.class).toProvider(PostgreSQLConfig.provider()).asEagerSingleton();
    bind(WebClientConfig.class).toProvider(WebClientConfig.provider()).asEagerSingleton();
  }

  private void bindClients() {
    bind(AerospikeClientImpl.class).in(Singleton.class);
    bind(AerospikeClient.class).to(AerospikeClientImpl.class);
    bind(PgReaderClientImpl.class).in(Singleton.class);
    bind(PgWriterClientImpl.class).in(Singleton.class);
    bind(PgReaderClient.class).to(PgReaderClientImpl.class);
    bind(PgWriterClient.class).to(PgWriterClientImpl.class);
    bind(WebClientImpl.class).in(Singleton.class);
    bind(WebClient.class).to(WebClientImpl.class);
  }

  private void bindDAOs() {
    bind(HealthCheckDAO.class).to(HealthCheckDAOImpl.class);
    bind(TagsDAO.class).to(TagsDAOImpl.class);
  }

  private void bindServices() {
    bind(HealthCheckService.class).to(HealthCheckServiceImpl.class);
    bind(TagsService.class).to(TagsServiceImpl.class);
  }
}
