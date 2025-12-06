package com.ascend.testlab.config.provider;

import com.ascend.testlab.util.ConfigUtil;
import com.google.inject.Provider;
import lombok.Getter;

/**
 * Provider for the config. Uses the ConfigUtil to get the config from the config file and load it
 * into a pojo.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @param <T> the type of the config
 */
@Getter
public class ConfigProvider<T> implements Provider<T> {

  /** The name of the config directory. */
  private final String configDirectory;

  /** The class of the config. */
  private final Class<T> clazz;

  /** The config path format. */
  protected String configPathFormat = "config/%s/%%s.conf";

  /**
   * Constructor for the ConfigProvider.
   *
   * @param configDirectory the config directory
   * @param clazz the class of the config
   */
  public ConfigProvider(String configDirectory, Class<T> clazz) {
    this.configDirectory = configDirectory;
    this.clazz = clazz;
  }

  /** {@inheritDoc} */
  @Override
  public T get() {
    return ConfigUtil.getTypedConfigFromConfigFile(getConfigPath(), clazz);
  }

  /**
   * Get the config path.
   *
   * @return the config path
   */
  protected String getConfigPath() {
    return configPathFormat.formatted(configDirectory);
  }
}
