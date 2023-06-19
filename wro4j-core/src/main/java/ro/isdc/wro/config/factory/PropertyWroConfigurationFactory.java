/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Loads configurations from a {@link Properties} object.
 *
 * @author Alex Objelean
 * @author Paul Podgorsek
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
    config.setCacheGzippedContent(valueAsBoolean(ConfigConstants.cacheGzippedContent));
    config.setCacheHttpEnabled(valueAsBoolean(ConfigConstants.cacheHttpEnabled));
    config.setCacheUpdatePeriod(valueAsLong(ConfigConstants.cacheUpdatePeriod));
    config.setConnectionTimeout(valueAsInteger(ConfigConstants.connectionTimeout));
    config.setDebug(valueAsBoolean(ConfigConstants.debug));
    config.setEncoding(valueAsString(ConfigConstants.encoding));
    config.setGzipEnabled(valueAsBoolean(ConfigConstants.gzipResources));
    config.setHeader(valueAsString(ConfigConstants.header));
    config.setIgnoreEmptyGroup(valueAsBoolean(ConfigConstants.ignoreEmptyGroup));
    config.setIgnoreFailingProcessor(valueAsBoolean(ConfigConstants.ignoreFailingProcessor));
    config.setIgnoreMissingResources(valueAsBoolean(ConfigConstants.ignoreMissingResources));
    config.setJmxEnabled(valueAsBoolean(ConfigConstants.jmxEnabled));
    config.setMbeanName(valueAsString(ConfigConstants.mbeanName));
    config.setMinimizeEnabled(valueAsBoolean(ConfigConstants.minimizeEnabled));
    config.setModelUpdatePeriod(valueAsLong(ConfigConstants.modelUpdatePeriod));
    config.setParallelPreprocessing(valueAsBoolean(ConfigConstants.parallelPreprocessing));
    config.setResourceWatcherAsync(valueAsBoolean(ConfigConstants.resourceWatcherAsync));
    config.setResourceWatcherUpdatePeriod(valueAsLong(ConfigConstants.resourceWatcherUpdatePeriod));
    config.setWroManagerClassName(valueAsString(ConfigConstants.managerFactoryClassName));
    
    LOG.debug("WroConfiguration created: {}", config);
    
    return config;
  }
  
  private Integer valueAsInteger(final ConfigConstants configuration) {
    
    String stringValue = valueAsString(configuration);
    
    if (stringValue == null) {
      return null;
    } else {
      try {
        return Integer.valueOf(stringValue);
      } catch (final NumberFormatException e) {
        final String message = "Invalid Integer value: " + stringValue;
        LOG.error(message);
        throw new WroRuntimeException(message);
      }
    }
  }
  
  private Long valueAsLong(final ConfigConstants configuration) {
    
    String stringValue = valueAsString(configuration);
    
    if (stringValue == null) {
      return null;
    } else {
      try {
        return Long.valueOf(stringValue);
      } catch (final NumberFormatException e) {
        final String message = "Invalid Long value: " + stringValue;
        LOG.error(message);
        throw new WroRuntimeException(message);
      }
    }
  }
  
  private Boolean valueAsBoolean(final ConfigConstants configuration) {
    return BooleanUtils.toBooleanObject(valueAsString(configuration));
  }
  
  /**
   * @return The string representation of the corresponding property value.
   */
  private String valueAsString(final ConfigConstants configuration) {
    
    if (configuration != null) {
      Object propertyValue = properties.get(configuration.getPropertyKey());
      
      if (propertyValue != null) {
        return StringUtils.trimToNull(String.valueOf(propertyValue));
      }
    }
    
    return null;
  }
  
}
