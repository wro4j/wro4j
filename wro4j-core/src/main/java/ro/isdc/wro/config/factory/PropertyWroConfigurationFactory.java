/*
 * Copyright (C) 2011 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;

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
  private Properties properties;

  /**
   * {@inheritDoc}
   */
  public WroConfiguration create() {
    final WroConfiguration config = new WroConfiguration();
    if (properties != null) {
      config.setDebug(valueAsBoolean(properties.get("debug"), true));
      config.setGzipEnabled(valueAsBoolean(properties.get("gzipResources"), true));
      config.setJmxEnabled(valueAsBoolean(properties.get("jmxEnabled"), true));
      config.setCacheUpdatePeriod(valueAsLong("cacheUpdatePeriod", 0));
      config.setModelUpdatePeriod(valueAsLong("modelUpdatePeriod", 0));
      config.setDisableCache(valueAsBoolean(properties.get("disableCache"), false));
      config.setIgnoreMissingResources(valueAsBoolean(properties.get("ignoreMissingResources"), true));
    }
    return config;
  }

  private long valueAsLong(final Object object, final long defaultValue) {
    final String stringValue = valueAsString(object);
    return stringValue != null ? Long.valueOf(stringValue) : defaultValue;
  }

  private boolean valueAsBoolean(final Object object, final boolean defaultValue) {
    return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean(valueAsString(object)), true);
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
