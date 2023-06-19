/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterConfig;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.config.support.DeploymentMode;
import ro.isdc.wro.config.support.PropertiesFactory;
import ro.isdc.wro.util.ObjectFactory;


/**
 * This factory retrieve wroConfigurations from {@link FilterConfig}. The underlying implementation creates a
 * {@link Properties} object from {@link FilterConfig} and populates it with all {@link ConfigConstants} values and
 * creates the {@link WroConfiguration} using {@link PropertyWroConfigurationFactory}.
 *
 * @author Alex Objelean
 * @since 1.3.7
 */
public class FilterConfigWroConfigurationFactory
  implements ObjectFactory<WroConfiguration>, PropertiesFactory {
  private static final Logger LOG = LoggerFactory.getLogger(FilterConfigWroConfigurationFactory.class);
  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode is used.
   */
  public static final String PARAM_CONFIGURATION = "configuration";

  /**
   * Filter configuration from where init-params will be read in order to create {@link WroConfiguration} object.
   */
  private final FilterConfig filterConfig;

  public FilterConfigWroConfigurationFactory(final FilterConfig filterConfig) {
    notNull(filterConfig);
    this.filterConfig = filterConfig;
  }

  /**
   * Prepares the {@link Properties} object before it is used by the {@link PropertyWroConfigurationFactory}.
   * @return {@link Properties} object used by {@link PropertyWroConfigurationFactory} to create
   *         {@link WroConfiguration}
   */
  public Properties createProperties() {
    return createPropertiesFromFilterConfig();
  }

  /**
   * @return initialized {@link Properties} object based init params found in {@link FilterConfig}.
   */
  protected final Properties createPropertiesFromFilterConfig() {
    final Properties props = new Properties();
    for (final ConfigConstants config : ConfigConstants.values()) {
      final String value = filterConfig.getInitParameter(config.getPropertyKey());
      if (value != null) {
        LOG.debug("filterConfig initParam ({}), with value ({})", config.getPropertyKey(), value);
        props.setProperty(config.getPropertyKey(), value);
      }
    }
    // add support for "configuration" init-param for backward compatibility.
    final String configurationType = filterConfig.getInitParameter(PARAM_CONFIGURATION);
    if (StringUtils.isNotEmpty(configurationType)) {
      props.setProperty(ConfigConstants.debug.getPropertyKey(), String.valueOf(isDebug(configurationType)));
    }
    return props;
  }

  /**
   * @return true if the "configuration" init-param is not "DEPLOYMENT"
   */
  private boolean isDebug(final String configurationType) {
    return !DeploymentMode.DEPLOYMENT.toString().equalsIgnoreCase(configurationType);
  }

  /**
   * {@inheritDoc}
   */
  public final WroConfiguration create() {
    return new PropertyWroConfigurationFactory(createProperties()).create();
  }
}
