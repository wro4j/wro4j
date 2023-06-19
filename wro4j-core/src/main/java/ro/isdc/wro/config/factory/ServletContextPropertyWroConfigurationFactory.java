/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Load {@link WroConfiguration} from a servletContext relative location. By default loads properties file from (
 * <code>/WEB-INF/wro.properties</code>) location.
 * 
 * @author Alex Objelean
 * @since 1.4.6
 */
public class ServletContextPropertyWroConfigurationFactory
    implements ObjectFactory<WroConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextPropertyWroConfigurationFactory.class);
  /**
   * The default name of the properties file holding wro configurations.
   */
  private static final String DEFAULT_PROPERTIES_FILE_NAME = "/WEB-INF/wro.properties";
  private ServletContext servletContext;

  public ServletContextPropertyWroConfigurationFactory(final ServletContext servletContext) {
    Validate.notNull(servletContext);
    this.servletContext = servletContext;
  }

  /**
   * @return default path to configuration file relative to {@link ServletContext} location.
   */
  protected String getConfigPath() {
    return DEFAULT_PROPERTIES_FILE_NAME;
  }
  
  /**
   * {@inheritDoc}
   */
  public final WroConfiguration create() {
    return new PropertyWroConfigurationFactory(createProperties()).create();
  }
  
  /**
   * @return {@link Properties} loaded from the stream from servletContext location specified by
   *         {@link ServletContextPropertyWroConfigurationFactory#getConfigPath()} method.
   */
  public Properties createProperties() {

    // Merge Properties file content with the filterConfig content.
    final Properties props = new Properties();

    try (InputStream propertyStream = servletContext.getResourceAsStream(getConfigPath())) {
      LOG.debug("loading config resource from: {}", getConfigPath());
      Validate.notNull(propertyStream);
      props.load(propertyStream);
    } catch (final Exception e) {
      LOG.warn("[WARN] Cannot read properties file stream from default location: {}. Using default configuration.", getConfigPath());
    }

    return props;
  }
}
