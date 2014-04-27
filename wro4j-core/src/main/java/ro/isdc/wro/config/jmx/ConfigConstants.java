/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.jmx;

import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * Hold the name of the properties.
 *
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
   * Parameter containing an integer value for specifying how often (in seconds) to run a thread responsible for
   * checking resource changes. When a change is detected, the cache for that particular group is invalidated.
   */
  resourceWatcherUpdatePeriod,
  /**
   * Flag which enables an experimental feature: asynchronous check for resource watcher.
   */
  resourceWatcherAsync,
  /**
   * Flag indicating if the minimization is enabled. When this flag is false, the minimization will be
   *         suppressed for all resources.
   */
  minimizeEnabled,
  /**
   * Disable cache configuration option. When true, the processed content won't be cached in DEVELOPMENT mode. In
   * DEPLOYMENT mode changing this flag will have no effect.
   */
  disableCache,
  /**
   * When true, will run in parallel pre processing of multiple resources. In theory this should improve the performance.
   */
  parallelPreprocessing,
  /**
   * When this flag is enabled, the raw processed content will be gzipped only the first time and all subsequent
   * requests will use the cached gzipped content. Otherwise, the gzip operation will be performed for each request.
   * This flag allow to control the memory vs processing power trade-off.
   */
  cacheGzippedContent,
  /**
   * Instructs wro4j to not throw an exception when a resource is missing.
   */
  ignoreMissingResources,
  /**
   * When a group is empty and this flag is false, the processing will fail. This is useful for runtime solution to
   * allow filter chaining when there is nothing to process for a given request.
   */
  ignoreEmptyGroup,
  /**
   * When this flag is true, any failure during processor will leave the content unchanged. Otherwise, the exception
   * will interrupt processing with a {@link RuntimeException}.
   */
  ignoreFailingProcessor,
  /**
   * Encoding to use when reading and writing bytes from/to stream
   */
  encoding,
  /**
   * The fully qualified class name of the {@link WroManagerFactory} implementation.
   */
  managerFactoryClassName,
  /**
   * the name of MBean to be used by JMX to configure wro4j.
   */
  mbeanName,
  /**
   * The parameter used to specify headers to put into the response, used mainly for caching.
   */
  header,
  /**
   * After how many seconds the connection to servlet context and external url will be timed-out. This is useful to
   * avoid memory leaks when connection pool responsible for cache and model reload is destroyed.
   */
  connectionTimeout
}