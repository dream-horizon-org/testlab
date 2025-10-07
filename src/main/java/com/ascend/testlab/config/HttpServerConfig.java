package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpServerConfig {
  private String host;
  private Integer port;
  private Integer compressionLevel;
  private Boolean compressionSupported;
  private Integer idleTimeout;
  private Boolean logActivity;
  private Boolean reusePort;
  private Boolean reuseAddress;
  private Boolean tcpFastOpen;
  private Boolean tcpNoDelay;
  private Boolean tcpQuickAck;
  private Boolean tcpKeepAlive;
  private Boolean useAlpn;

  public static ConfigProvider<HttpServerConfig> provider() {
    return new ConfigProvider<>("http-server", HttpServerConfig.class);
  }
}
