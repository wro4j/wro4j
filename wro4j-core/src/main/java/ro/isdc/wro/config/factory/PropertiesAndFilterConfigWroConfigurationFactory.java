/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterConfig;
import ro.isdc.wro.config.jmx.WroConfiguration;


/**
 * Uses a default Properties file location (under <code>/WEB-INF/wro.properties</code>) for creating
 * {@link WroConfiguration} and overrides them with properties defined in {@link FilterConfig} object. This will allow
 * user to easily switch from old style of configuring {@link WroConfiguration} to the new style (by defining a property
 * file).
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public class PropertiesAndFilterConfigWroConfigurationFactory
  extends FilterConfigWroConfigurationFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PropertiesAndFilterConfigWroConfigurationFactory.class);
  /**
   * The default factory used to load configuration from a default location.
   */
  private final ServletContextPropertyWroConfigurationFactory defaultFactory;

  public PropertiesAndFilterConfigWroConfigurationFactory(final FilterConfig filterConfig) {
    super(filterConfig);
    defaultFactory = new ServletContextPropertyWroConfigurationFactory(filterConfig.getServletContext());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Properties createProperties() {
    //Merge Properties file content with the filterConfig content.
    final Properties merged = new Properties();
    try {
      final Properties props = newDefaultProperties();
      if (props != null) {
        merged.putAll(props);
      }
    } catch (final Exception e) {
      LOG.warn("Cannot load properties from default location. Load propertis from filterConfig", e);
    }
    final Properties props = createPropertiesFromFilterConfig();
    merged.putAll(props);
    return merged;
  }

  /**
   * @return the {@link Properties} built from the default location.
   */
  protected Properties newDefaultProperties() {
    return defaultFactory.createProperties();
  }
}
