/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.jmx;

/**
 * Hold the name of the properties.
 * @author Alex Objelean
 * @created 11 May 2011
 * @since 1.3.7
 */
public enum ConfigConstants {
  debug,
  /**
   * Boolean flag for enable/disable resource gzipping.
   */
  gzipResources,
  /**
   * Parameter allowing to turn jmx on or off.
   */
  jmxEnabled,
  /**
   * Parameter containing an integer value for specifying how often (in seconds) the cache should be refreshed.
   */
  cacheUpdatePeriod,
  /**
   * Parameter containing an integer value for specifying how often (in seconds) the model should be refreshed.
   */
  modelUpdatePeriod,
  /**
   * Disable cache configuration option. When true, the processed content won't be cached in DEVELOPMENT mode. In
   * DEPLOYMENT mode changing this flag will have no effect.
   */
  disableCache,
  /**
   * Instructs wro4j to not throw an exception when a resource is missing.
   */
  ignoreMissingResources
}