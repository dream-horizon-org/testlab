package com.ascend.testlab.client.datadog.impl;

import com.ascend.testlab.client.datadog.DDClient;
import com.ascend.testlab.constants.datadog.DDConstants;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import io.reactivex.rxjava3.core.Completable;

public class DDClientImpl implements DDClient {
  private static final String DD_PREFIX =
      System.getenv().getOrDefault(DDConstants.SERVICE_NAME, DDConstants.DD_PREFIX);
  private static final String DD_HOST =
      System.getenv().getOrDefault(DDConstants.DD_AGENT_HOST, "localhost");
  private static final Integer DD_PORT = 8125;

  private final StatsDClient statsDClient;

  public DDClientImpl() {
    this.statsDClient =
        new NonBlockingStatsDClientBuilder()
            .prefix(DD_PREFIX)
            .hostname(DD_HOST)
            .port(DD_PORT)
            .build();
  }

  @Override
  public Completable close() {
    return Completable.fromAction(statsDClient::close);
  }

  @Override
  public void increment(String aspect, String... tags) {
    statsDClient.increment(aspect, tags);
  }

  @Override
  public <T extends Number> void gauge(String aspect, T value, String... tags) {
    if (value instanceof Long || value instanceof Integer)
      statsDClient.gauge(aspect, value.longValue(), tags);
    else if (value instanceof Double || value instanceof Float)
      statsDClient.gauge(aspect, value.doubleValue(), tags);
  }
}
