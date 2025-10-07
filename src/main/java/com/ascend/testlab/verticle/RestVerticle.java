package com.ascend.testlab.verticle;

import com.ascend.testlab.config.HttpServerConfig;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.injection.GuiceInjector;
import com.dream11.rest.AbstractRestVerticle;
import com.dream11.rest.ClassInjector;
import com.dream11.rest.provider.JsonProvider;
import com.dream11.rest.provider.impl.JacksonProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.core.http.HttpServerOptions;

public class RestVerticle extends AbstractRestVerticle {

  @Inject
  public RestVerticle(HttpServerConfig httpServerConfig) {
    super(Constants.PACKAGE_NAME, getHttpServerOptions(httpServerConfig));
  }

  @Override
  protected ClassInjector getInjector() {
    return GuiceInjector::getInstance;
  }

  @Override
  protected JsonProvider getJsonProvider() {
    return new JacksonProvider(this.getInjector().getInstance(ObjectMapper.class));
  }

  private static HttpServerOptions getHttpServerOptions(HttpServerConfig httpServerConfig) {
    return new HttpServerOptions()
        .setHost(httpServerConfig.getHost())
        .setPort(httpServerConfig.getPort())
        .setCompressionLevel(httpServerConfig.getCompressionLevel())
        .setCompressionSupported(httpServerConfig.getCompressionSupported())
        .setIdleTimeout(httpServerConfig.getIdleTimeout())
        .setLogActivity(httpServerConfig.getLogActivity())
        .setReusePort(httpServerConfig.getReusePort())
        .setReuseAddress(httpServerConfig.getReuseAddress())
        .setTcpFastOpen(httpServerConfig.getTcpFastOpen())
        .setTcpNoDelay(httpServerConfig.getTcpNoDelay())
        .setTcpQuickAck(httpServerConfig.getTcpQuickAck())
        .setTcpKeepAlive(httpServerConfig.getTcpKeepAlive())
        .setUseAlpn(httpServerConfig.getUseAlpn());
  }
}
