/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import javax.servlet.FilterConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.util.ObjectFactory;


/**
 * This factory retrieve wroConfigurations from {@link FilterConfig}. The underlying implementation creates a
 * {@link Properties} object from {@link FilterConfig} and populates it with all {@link ConfigConstants} values and
 * creates the {@link WroConfiguration} using {@link PropertyWroConfigurationFactory}.
 *
 * @author Alex Objelean
 * @created 13 May 2011
 * @since 1.3.7
 */
public class FilterConfigWroConfigurationFactory
  implements ObjectFactory<WroConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(FilterConfigWroConfigurationFactory.class);
  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode is used.
   */
  public static final String PARAM_CONFIGURATION = "configuration";
  /**
   * Replace with a boolean used for debug Deployment configuration option. If false, the DEVELOPMENT (or DEBUG) is
   * assumed.
   */
  // TODO deprecate and use a boolean value init-param
  @Deprecated
  public static final String PARAM_VALUE_DEPLOYMENT = "DEPLOYMENT";
  /**
   * Decorated factory.
   */
  private final PropertyWroConfigurationFactory factory;


  public FilterConfigWroConfigurationFactory(final FilterConfig filterConfig) {
    Validate.notNull(filterConfig);
    factory = new PropertyWroConfigurationFactory();
    factory.setProperties(createProps(filterConfig));
  }


  /**
   * @return initialized {@link Properties} object based init params found in {@link FilterConfig}.
   */
  private Properties createProps(final FilterConfig filterConfig) {
    final Properties props = new Properties();
    for (final ConfigConstants config : ConfigConstants.values()) {
      final String value = filterConfig.getInitParameter(config.name());
      if (value != null) {
        LOG.debug("filterConfig initParam ({}), with value ({})", config.name(), value);
        props.setProperty(config.name(), value);
      }
    }
    // add support for "configuration" init-param for backward compatibility.
    final String configurationType = filterConfig.getInitParameter(PARAM_CONFIGURATION);
    if (!StringUtils.isEmpty(configurationType)) {
      props.setProperty(ConfigConstants.debug.name(), String.valueOf(isDebug(configurationType)));
    }
    return props;
  }


  /**
   * @return true if the "configuration" init-param is not "DEPLOYMENT"
   */
  private boolean isDebug(final String configurationType) {
    return !PARAM_VALUE_DEPLOYMENT.equalsIgnoreCase(configurationType);
  }


  /**
   * {@inheritDoc}
   */
  public WroConfiguration create() {
    return factory.create();
  }
}
