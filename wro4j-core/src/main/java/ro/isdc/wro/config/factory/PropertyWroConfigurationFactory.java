/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * Loads configurations from a {@link Properties} object.
 *
 * @author Alex Objelean
 * @created 10 May 2011
 * @since 1.3.7
 */
public class PropertyWroConfigurationFactory
    implements WroConfigurationFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestPropertyWroConfigurationFactory.class);
  /**
   * Holds configuration options. If no properties are set, the default values will be used instead.
   */
  private Properties properties;

  /**
   * {@inheritDoc}
   */
  public WroConfiguration create() {
    final WroConfiguration config = new WroConfiguration();
    if (properties != null) {
      config.setDebug(valueAsBoolean(properties.get(ConfigConstants.debug.name()), true));
      config.setGzipEnabled(valueAsBoolean(properties.get(ConfigConstants.gzipResources.name()), true));
      config.setJmxEnabled(valueAsBoolean(properties.get(ConfigConstants.jmxEnabled.name()), true));
      config.setCacheUpdatePeriod(valueAsLong(properties.get(ConfigConstants.cacheUpdatePeriod.name()), 0));
      config.setModelUpdatePeriod(valueAsLong(properties.get(ConfigConstants.modelUpdatePeriod.name()), 0));
      config.setDisableCache(valueAsBoolean(properties.get(ConfigConstants.disableCache.name()), false));
      config.setIgnoreMissingResources(valueAsBoolean(properties.get(ConfigConstants.ignoreMissingResources.name()), true));
    }
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
    return object != null ? String.valueOf(object) : null;
  }

  /**
   * @param props the props to set
   */
  public void setProperties(final Properties props) {
    this.properties = props;
  }
}
