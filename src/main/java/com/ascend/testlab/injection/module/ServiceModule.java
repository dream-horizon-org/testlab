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
import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.dao.VariantCountDAO;
import com.ascend.testlab.dao.impl.AdminDAOImpl;
import com.ascend.testlab.dao.impl.AllocationDAOImpl;
import com.ascend.testlab.dao.impl.ExperimentDAOImpl;
import com.ascend.testlab.dao.impl.HealthCheckDAOImpl;
import com.ascend.testlab.dao.impl.VariantCountDAOImpl;
import com.ascend.testlab.service.AdminService;
import com.ascend.testlab.service.AllocationService;
import com.ascend.testlab.service.CohortService;
import com.ascend.testlab.service.ExperimentService;
import com.ascend.testlab.service.HealthCheckService;
import com.ascend.testlab.service.impl.AdminServiceImpl;
import com.ascend.testlab.service.impl.AllocationServiceImpl;
import com.ascend.testlab.service.impl.CohortServiceImpl;
import com.ascend.testlab.service.impl.ExperimentServiceImpl;
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
    bind(PostgreSQLConfig.class).toProvider(PostgreSQLConfig.provider()).asEagerSingleton();
    bind(WebClientConfig.class).toProvider(WebClientConfig.provider()).asEagerSingleton();
  }

  /** Bind the client interfaces to their implementations. */
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

  /** Bind the DAO interfaces to their implementations. */
  private void bindDAOs() {
    bind(HealthCheckDAO.class).to(HealthCheckDAOImpl.class);
    bind(AdminDAO.class).to(AdminDAOImpl.class);
    bind(ExperimentDAO.class).to(ExperimentDAOImpl.class);
    bind(AllocationDAO.class).to(AllocationDAOImpl.class);
    bind(VariantCountDAO.class).to(VariantCountDAOImpl.class);
  }

  /** Bind the service interfaces to their implementations. */
  private void bindServices() {
    bind(HealthCheckService.class).to(HealthCheckServiceImpl.class);
    bind(AdminService.class).to(AdminServiceImpl.class);
    bind(ExperimentService.class).to(ExperimentServiceImpl.class);
    bind(AllocationService.class).to(AllocationServiceImpl.class);
    bind(CohortService.class).to(CohortServiceImpl.class);
  }
}
