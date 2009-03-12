/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;

import ro.isdc.wro.util.Configuration;

/**
 * ConfigurationOptions. Holds different configuration informations regarding
 * the way framework must behave.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 18, 2008
 */
public final class WroSettings {
  /**
   * Configuration by default is DEVELOPMENT.
   */
  private static Configuration configuration = Configuration.DEVELOPMENT;

  /**
   * Private constructor. Prevent instantiation.
   */
  private WroSettings() {}

  /**
   * @return the configuration
   */
  public static Configuration getConfiguration() {
    return configuration;
  }

  /**
   * @param config
   *          the config to set
   */
  public static void setConfiguration(final Configuration config) {
    configuration = config;
  }
}
