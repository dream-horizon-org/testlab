package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import com.typesafe.config.Optional;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetworkOptions;
import io.vertx.core.net.TCPSSLOptions;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the HTTP server of the application.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class HttpServerConfig {
  private static final String DEFAULT_PORT = "8080";

  /** The host for the HTTP server. */
  @Optional private String host = NetServerOptions.DEFAULT_HOST;

  /** The port for the HTTP server. */
  @Optional private Integer port;

  /** The compression level for the HTTP server. */
  @Optional private Integer compressionLevel = HttpServerOptions.DEFAULT_COMPRESSION_LEVEL;

  /** Whether compression is supported for the HTTP server. */
  @Optional private Boolean compressionSupported = HttpServerOptions.DEFAULT_COMPRESSION_SUPPORTED;

  /** The idle timeout for the HTTP server. */
  @Optional private Integer idleTimeout = TCPSSLOptions.DEFAULT_IDLE_TIMEOUT;

  /** Whether log activity is enabled for the HTTP server. */
  @Optional private Boolean logActivity = NetworkOptions.DEFAULT_LOG_ENABLED;

  /** Whether reuse port is enabled for the HTTP server. */
  @Optional private Boolean reusePort = NetworkOptions.DEFAULT_REUSE_PORT;

  /** Whether reuse address is enabled for the HTTP server. */
  @Optional private Boolean reuseAddress = NetworkOptions.DEFAULT_REUSE_ADDRESS;

  /** Whether TCP fast open is enabled for the HTTP server. */
  @Optional private Boolean tcpFastOpen = TCPSSLOptions.DEFAULT_TCP_FAST_OPEN;

  /** Whether TCP no delay is enabled for the HTTP server. */
  @Optional private Boolean tcpNoDelay = TCPSSLOptions.DEFAULT_TCP_NO_DELAY;

  /** Whether TCP quick ack is enabled for the HTTP server. */
  @Optional private Boolean tcpQuickAck = TCPSSLOptions.DEFAULT_TCP_QUICKACK;

  /** Whether TCP keep alive is enabled for the HTTP server. */
  @Optional private Boolean tcpKeepAlive = TCPSSLOptions.DEFAULT_TCP_KEEP_ALIVE;

  /** Whether ALPN is enabled for the HTTP server. */
  @Optional private Boolean useAlpn = TCPSSLOptions.DEFAULT_USE_ALPN;

  /**
   * Get the port for the HTTP server. If not set, use the default port.
   *
   * @return the port for the HTTP server
   */
  public Integer getPort() {
    return 0 < this.port
        ? this.port
        : Integer.parseInt(System.getProperty("http.default.port", DEFAULT_PORT));
  }

  /**
   * Get the provider for the HTTP server config.
   *
   * @return the provider for the HTTP server config
   * @see ConfigProvider
   */
  public static ConfigProvider<HttpServerConfig> provider() {
    return new ConfigProvider<>("http-server", HttpServerConfig.class);
  }
}
