/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Loads configurations from a {@link Properties} object.
 *
 * @author Alex Objelean
 * @created 10 May 2011
 * @since 1.3.7
 */
public class PropertyWroConfigurationFactory
    implements ObjectFactory<WroConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyWroConfigurationFactory.class);
  /**
   * Holds configuration options. If no properties are set, the default values will be used instead.
   */
  private final Properties properties;

  public PropertyWroConfigurationFactory() {
    this(new Properties());
  }

  public PropertyWroConfigurationFactory(final Properties props) {
    Validate.notNull(props);
    this.properties = props;
  }

  public WroConfiguration create() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(valueAsBoolean(properties.get(ConfigConstants.debug.name()), true));
    config.setGzipEnabled(valueAsBoolean(properties.get(ConfigConstants.gzipResources.name()), true));
    config.setJmxEnabled(valueAsBoolean(properties.get(ConfigConstants.jmxEnabled.name()), true));
    config.setCacheUpdatePeriod(valueAsLong(properties.get(ConfigConstants.cacheUpdatePeriod.name()), 0));
    config.setModelUpdatePeriod(valueAsLong(properties.get(ConfigConstants.modelUpdatePeriod.name()), 0));
    config.setResourceWatcherUpdatePeriod(valueAsLong(
        properties.get(ConfigConstants.resourceWatcherUpdatePeriod.name()), 0));
    config.setResourceWatcherAsync(valueAsBoolean(properties.get(ConfigConstants.resourceWatcherAsync.name()), false));
    config.setMinimizeEnabled(valueAsBoolean(properties.get(ConfigConstants.minimizeEnabled.name()), true));
    config.setIgnoreMissingResources(valueAsBoolean(properties.get(ConfigConstants.ignoreMissingResources.name()), true));
    config.setIgnoreEmptyGroup(valueAsBoolean(properties.get(ConfigConstants.ignoreEmptyGroup.name()), true));
    config.setIgnoreFailingProcessor(valueAsBoolean(properties.get(ConfigConstants.ignoreFailingProcessor.name()), false));
    config.setEncoding(valueAsString(properties.get(ConfigConstants.encoding.name()), WroConfiguration.DEFAULT_ENCODING));
    config.setWroManagerClassName(valueAsString(properties.get(ConfigConstants.managerFactoryClassName.name())));
    config.setMbeanName(valueAsString(properties.get(ConfigConstants.mbeanName.name())));
    config.setHeader(valueAsString(properties.get(ConfigConstants.header.name())));
    config.setCacheGzippedContent(valueAsBoolean(properties.get(ConfigConstants.cacheGzippedContent.name()), false));
    config.setParallelPreprocessing(valueAsBoolean(properties.get(ConfigConstants.parallelPreprocessing.name()), false));
    config.setConnectionTimeout((int) valueAsLong(properties.get(ConfigConstants.connectionTimeout.name()),
        WroConfiguration.DEFAULT_CONNECTION_TIMEOUT));
    LOG.debug("WroConfiguration created: {}", config);
    return config;
  }

  private long valueAsLong(final Object object, final long defaultValue) {
    if (object == null) {
      return defaultValue;
    }
    try {
      return Long.valueOf(valueAsString(object));
    } catch (final NumberFormatException e) {
      final String message = "Invalid long value: " + object + ". Using defaultValue: " + defaultValue;
      LOG.error(message);
      throw new WroRuntimeException(message);
    }
  }

  private boolean valueAsBoolean(final Object object, final boolean defaultValue) {
    return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(valueAsString(object)), defaultValue);
  }

  /**
   * Helps to avoid "null" as string situation.
   */
  private String valueAsString(final Object object) {
    return valueAsString(object, null);
  }

  /**
   * @return string representation of an object. If the object is null the defaultValue will be returned.
   */
  private String valueAsString(final Object object, final String defaultValue) {
    return object != null ? String.valueOf(object) : defaultValue;
  }
}
