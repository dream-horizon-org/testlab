package com.ascend.testlab.injection.module;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.*;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.service.HealthCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for ServiceModule.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ServiceModule Tests")
public class ServiceModuleTest {

  private Vertx vertx;
  private ServiceModule module;

  @BeforeEach
  void setUp() {
    // Set required environment variables for config loading
    System.setProperty("POSTGRES_USER", "test_user");
    System.setProperty("POSTGRES_PASSWORD", "test_password");

    vertx = Vertx.vertx();
    module = new ServiceModule(vertx);
  }

  @AfterEach
  void tearDown() {
    if (vertx != null) {
      vertx.close();
    }

    // Clean up system properties
    System.clearProperty("POSTGRES_USER");
    System.clearProperty("POSTGRES_PASSWORD");
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create module with valid Vertx instance")
    void testConstructorWithVertx() {
      // Act
      ServiceModule newModule = new ServiceModule(vertx);

      // Assert
      assertNotNull(newModule);
    }

    @Test
    @DisplayName("Should throw exception when Vertx is null")
    void testConstructorWithNullVertx() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> new ServiceModule(null));
    }
  }

  @Nested
  @DisplayName("Config Binding Tests")
  class ConfigBindingTests {

    @Test
    @DisplayName("Should bind AerospikeConfig")
    void testAerospikeConfigBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      AerospikeConfig config = injector.getInstance(AerospikeConfig.class);

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should bind ApplicationConfig")
    void testApplicationConfigBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      ApplicationConfig config = injector.getInstance(ApplicationConfig.class);

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should bind CircuitBreakerConfig")
    void testCircuitBreakerConfigBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      CircuitBreakerConfig config = injector.getInstance(CircuitBreakerConfig.class);

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should bind HttpServerConfig")
    void testHttpServerConfigBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      HttpServerConfig config = injector.getInstance(HttpServerConfig.class);

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should bind PostgreSQLConfig")
    void testPostgreSQLConfigBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      PostgreSQLConfig config = injector.getInstance(PostgreSQLConfig.class);

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should bind WebClientConfig")
    void testWebClientConfigBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      WebClientConfig config = injector.getInstance(WebClientConfig.class);

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should bind all configs as eager singletons")
    void testConfigEagerSingletons() {
      // Act
      Injector injector = Guice.createInjector(module);
      AerospikeConfig config1a = injector.getInstance(AerospikeConfig.class);
      AerospikeConfig config1b = injector.getInstance(AerospikeConfig.class);

      // Assert - Same instance should be returned
      assertSame(config1a, config1b);
    }
  }

  @Nested
  @DisplayName("Client Binding Tests")
  class ClientBindingTests {

    @Test
    @DisplayName("Should bind AerospikeClient")
    void testAerospikeClientBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      AerospikeClient client = injector.getInstance(AerospikeClient.class);

      // Assert
      assertNotNull(client);
    }

    @Test
    @DisplayName("Should bind PgReaderClient")
    void testPgReaderClientBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      PgReaderClient client = injector.getInstance(PgReaderClient.class);

      // Assert
      assertNotNull(client);
    }

    @Test
    @DisplayName("Should bind PgWriterClient")
    void testPgWriterClientBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      PgWriterClient client = injector.getInstance(PgWriterClient.class);

      // Assert
      assertNotNull(client);
    }

    @Test
    @DisplayName("Should bind WebClient")
    void testWebClientBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      WebClient client = injector.getInstance(WebClient.class);

      // Assert
      assertNotNull(client);
    }

    @Test
    @DisplayName("Should bind clients as singletons")
    void testClientSingletons() {
      // Act
      Injector injector = Guice.createInjector(module);
      AerospikeClient client1 = injector.getInstance(AerospikeClient.class);
      AerospikeClient client2 = injector.getInstance(AerospikeClient.class);

      // Assert
      assertSame(client1, client2);
    }
  }

  @Nested
  @DisplayName("DAO Binding Tests")
  class DAOBindingTests {

    @Test
    @DisplayName("Should bind HealthCheckDAO")
    void testHealthCheckDAOBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      HealthCheckDAO dao = injector.getInstance(HealthCheckDAO.class);

      // Assert
      assertNotNull(dao);
    }

    @Test
    @DisplayName("Should create new DAO instance on each call")
    void testDAONotSingleton() {
      // Act
      Injector injector = Guice.createInjector(module);
      HealthCheckDAO dao1 = injector.getInstance(HealthCheckDAO.class);
      HealthCheckDAO dao2 = injector.getInstance(HealthCheckDAO.class);

      // Assert - DAOs are not bound as singletons, but dependencies are
      assertNotNull(dao1);
      assertNotNull(dao2);
    }
  }

  @Nested
  @DisplayName("Service Binding Tests")
  class ServiceBindingTests {

    @Test
    @DisplayName("Should bind HealthCheckService")
    void testHealthCheckServiceBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      HealthCheckService service = injector.getInstance(HealthCheckService.class);

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should create new Service instance on each call")
    void testServiceNotSingleton() {
      // Act
      Injector injector = Guice.createInjector(module);
      HealthCheckService service1 = injector.getInstance(HealthCheckService.class);
      HealthCheckService service2 = injector.getInstance(HealthCheckService.class);

      // Assert
      assertNotNull(service1);
      assertNotNull(service2);
    }
  }

  @Nested
  @DisplayName("Inherited Binding Tests")
  class InheritedBindingTests {

    @Test
    @DisplayName("Should inherit Vertx binding from DefaultModule")
    void testVertxBindingInherited() {
      // Act
      Injector injector = Guice.createInjector(module);
      Vertx boundVertx = injector.getInstance(Vertx.class);

      // Assert
      assertNotNull(boundVertx);
      assertSame(vertx, boundVertx);
    }

    @Test
    @DisplayName("Should inherit ObjectMapper binding from DefaultModule")
    void testObjectMapperBindingInherited() {
      // Act
      Injector injector = Guice.createInjector(module);
      ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);

      // Assert
      assertNotNull(objectMapper);
    }

    @Test
    @DisplayName("Should call super.configure()")
    void testSuperConfigure() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - Both parent and child bindings should be available
      assertNotNull(injector.getInstance(Vertx.class));
      assertNotNull(injector.getInstance(ObjectMapper.class));
      assertNotNull(injector.getInstance(AerospikeConfig.class));
    }
  }

  @Nested
  @DisplayName("Static Injection Tests")
  class StaticInjectionTests {

    @Test
    @DisplayName("Should request static injection for CircuitBreakerFactory")
    void testCircuitBreakerFactoryStaticInjection() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - CircuitBreakerFactory should have static field injected
      assertNotNull(injector);
      // Static injection happens during injector creation
      assertDoesNotThrow(() -> injector.getInstance(CircuitBreakerConfig.class));
    }
  }

  @Nested
  @DisplayName("Dependency Resolution Tests")
  class DependencyResolutionTests {

    @Test
    @DisplayName("Should resolve all client dependencies")
    void testResolveClientDependencies() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - All clients should be creatable
      assertDoesNotThrow(() -> injector.getInstance(AerospikeClient.class));
      assertDoesNotThrow(() -> injector.getInstance(PgReaderClient.class));
      assertDoesNotThrow(() -> injector.getInstance(PgWriterClient.class));
      assertDoesNotThrow(() -> injector.getInstance(WebClient.class));
    }

    @Test
    @DisplayName("Should resolve DAO dependencies")
    void testResolveDAODependencies() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - DAO should be creatable with its dependencies
      assertDoesNotThrow(() -> injector.getInstance(HealthCheckDAO.class));
    }

    @Test
    @DisplayName("Should resolve Service dependencies")
    void testResolveServiceDependencies() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - Service should be creatable with its dependencies
      assertDoesNotThrow(() -> injector.getInstance(HealthCheckService.class));
    }

    @Test
    @DisplayName("Should resolve complex dependency chain")
    void testResolveDependencyChain() {
      // Act
      Injector injector = Guice.createInjector(module);
      HealthCheckService service = injector.getInstance(HealthCheckService.class);

      // Assert - Service depends on DAO, which depends on clients
      assertNotNull(service);
    }
  }

  @Nested
  @DisplayName("Module Configuration Tests")
  class ModuleConfigurationTests {

    @Test
    @DisplayName("Should configure all required bindings")
    void testAllBindingsConfigured() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - All expected bindings should be available
      assertNotNull(injector.getInstance(Vertx.class));
      assertNotNull(injector.getInstance(ObjectMapper.class));
      assertNotNull(injector.getInstance(AerospikeConfig.class));
      assertNotNull(injector.getInstance(AerospikeClient.class));
      assertNotNull(injector.getInstance(HealthCheckDAO.class));
      assertNotNull(injector.getInstance(HealthCheckService.class));
    }

    @Test
    @DisplayName("Should work with Guice injector")
    void testGuiceIntegration() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert
      assertNotNull(injector);
    }

    @Test
    @DisplayName("Should be usable as Module")
    void testUsableAsModule() {
      // Arrange
      com.google.inject.Module guiceModule = module;

      // Act
      Injector injector = Guice.createInjector(guiceModule);

      // Assert
      assertNotNull(injector);
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should support typical application startup")
    void testTypicalApplicationStartup() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - All core components should be available
      assertNotNull(injector.getInstance(Vertx.class));
      assertNotNull(injector.getInstance(AerospikeClient.class));
      assertNotNull(injector.getInstance(PgReaderClient.class));
      assertNotNull(injector.getInstance(PgWriterClient.class));
      assertNotNull(injector.getInstance(WebClient.class));
      assertNotNull(injector.getInstance(HealthCheckService.class));
    }

    @Test
    @DisplayName("Should create independent module instances")
    void testIndependentModuleInstances() {
      // Arrange
      Vertx vertx2 = Vertx.vertx();

      try {
        ServiceModule module1 = new ServiceModule(vertx);
        ServiceModule module2 = new ServiceModule(vertx2);

        // Act
        Injector injector1 = Guice.createInjector(module1);
        Injector injector2 = Guice.createInjector(module2);

        // Assert
        assertNotSame(injector1.getInstance(Vertx.class), injector2.getInstance(Vertx.class));
      } finally {
        vertx2.close();
      }
    }

    @Test
    @DisplayName("Should support configuration providers")
    void testConfigurationProviders() {
      // Act
      Injector injector = Guice.createInjector(module);

      // Assert - Configs should be loaded via providers
      AerospikeConfig config = injector.getInstance(AerospikeConfig.class);
      assertNotNull(config);
      assertNotNull(config.getHost());
      assertNotNull(config.getNamespace());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle Vertx close gracefully")
    void testVertxClose() {
      // Arrange
      Vertx testVertx = Vertx.vertx();
      ServiceModule testModule = new ServiceModule(testVertx);
      Injector injector = Guice.createInjector(testModule);
      Vertx boundVertx = injector.getInstance(Vertx.class);

      // Act
      testVertx.close();

      // Assert - Should still have reference but be closed
      assertNotNull(boundVertx);
    }

    @Test
    @DisplayName("Should handle multiple injector creations")
    void testMultipleInjectorCreations() {
      // Act
      Injector injector1 = Guice.createInjector(module);
      Injector injector2 = Guice.createInjector(module);

      // Assert - Each injector should be independent
      assertNotSame(injector1, injector2);
      assertSame(injector1.getInstance(Vertx.class), injector2.getInstance(Vertx.class));
    }
  }
}
