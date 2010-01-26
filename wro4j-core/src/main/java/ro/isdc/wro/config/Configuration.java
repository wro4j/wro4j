/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration. There are two types: development used for debugging (no
 * caching) & deployment used in production (with caching).
 *
 * @author Alex Objelean
 * @created Created on Nov 18, 2008
 */
public enum Configuration {
  DEPLOYMENT, DEVELOPMENT;
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

  /**
   * @return true if configuration is in DEVELOPMENT mode.
   */
  public boolean isDevelopment() {
    return this == DEVELOPMENT;
  }

  /**
   * @return true if configuration is in DEPLOYMENT mode.
   */
  public boolean isDeployment() {
    return this == DEPLOYMENT;
  }

  /**
   * Fail safe variant of valueOf method. Will use a DEPLOYMENT by default if passed value is invalid.
   * @param value string representation of configuration.
   * @return {@link Configuration} associated with value or Configuration.DEPLOYMENT if value is invalid.
   */
  public static Configuration of(final String value) {
    Configuration config = DEVELOPMENT;
    try {
      config = valueOf(value.toUpperCase());
    } catch(final Exception e) {
      LOG.warn("Invalid configuration value: " + value + ". Using by default " + config.name());
    }
    return config;
  }
}
