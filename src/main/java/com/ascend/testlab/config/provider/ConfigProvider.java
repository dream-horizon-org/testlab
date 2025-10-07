package com.ascend.testlab.config.provider;

import com.ascend.testlab.util.ConfigUtil;
import com.google.inject.Provider;
import lombok.Getter;

@Getter
public class ConfigProvider<T> implements Provider<T> {

  private final String configDirectory;
  private final Class<T> clazz;
  protected String configPathFormat = "config/%s/%%s.conf";

  public ConfigProvider(String configDirectory, Class<T> clazz) {
    this.configDirectory = configDirectory;
    this.clazz = clazz;
  }

  @Override
  public T get() {
    return ConfigUtil.getTypedConfigFromConfigFile(getConfigPath(), clazz);
  }

  protected String getConfigPath() {
    return configPathFormat.formatted(configDirectory);
  }
}
