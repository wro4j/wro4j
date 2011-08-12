/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.FilterConfig;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;


/**
 * Uses a default Properties file location (under <code>/WEB-INF/wro.properties</code>) for creating
 * {@link WroConfiguration} and overrides them with properties defined in {@link FilterConfig} object. This will allow
 * user to easily switch from old style of configuring {@link WroConfiguration} to the new style (by defining a property
 * file).
 *
 * @author Alex Objelean
 * @created 17 Jun 2011
 * @since 1.3.8
 */
public class PropertiesAndFilterConfigWroConfigurationFactory
  extends FilterConfigWroConfigurationFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PropertiesAndFilterConfigWroConfigurationFactory.class);
  /**
   * The default name of the properties file holding wro configurations.
   */
  private static final String DEFAULT_PROPERTIES_FILE_NAME = "wro.properties";

  public PropertiesAndFilterConfigWroConfigurationFactory(final FilterConfig filterConfig) {
    super(filterConfig);
  }

  /**
   * @return the stream of the {@link Properties} file containing configurations. This method can also return null.
   * @throws IOException if the stream could not be retrieved.
   */
  protected InputStream newPropertyStream() throws IOException {
    return defaultConfigPropertyStream(filterConfig);
  }

  /**
   * This method is static in order to be used in other classes, like {@link ConfigurableWroManagerFactory}.
   *
   * @return the stream of the default {@link Properties} file used to load configuration.
   */
  public static InputStream defaultConfigPropertyStream(final FilterConfig filterConfig) {
    Validate.notNull(filterConfig);
    return filterConfig.getServletContext().getResourceAsStream("/WEB-INF/" + DEFAULT_PROPERTIES_FILE_NAME);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Properties initProperties() {
    //Merge Properties file content with the filterConfig content.
    final Properties merged = new Properties();
    try {
      final InputStream propertyStream = newPropertyStream();
      if (propertyStream != null) {
        final Properties props = new Properties();
        props.load(propertyStream);
        merged.putAll(props);
      } else {
        LOG.debug("No Properties stream found, proceeding with reading init-params");
      }
    } catch (final IOException e) {
      LOG.error("Cannot read properties file stream", e);
      throw new WroRuntimeException("Invalid properties file provided", e);
    }
    final Properties props = createPropertiesFromFilterConfig();
    merged.putAll(props);
    return merged;
  }
}
