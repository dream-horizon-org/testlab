package com.ascend.testlab.client.datadog;

import io.reactivex.rxjava3.core.Completable;

public interface DDClient {
  Completable close();

  void increment(String aspect, String... tags);

  <T extends Number> void gauge(String aspect, T value, String... tags);
}
