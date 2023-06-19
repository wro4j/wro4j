/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.config.support;

import java.nio.charset.StandardCharsets;

import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * Hold the name of the properties. The default values are also managed here, this allows for their centralised
 * management.
 *
 * @author Alex Objelean
 * @author Paul Podgorsek
 * @since 1.3.7
 */
public enum ConfigConstants {
  
  /**
   * When this flag is enabled, the raw processed content will be gzipped only the first time and all subsequent
   * requests will use the cached gzipped content. Otherwise, the gzip operation will be performed for each request.
   * This flag allow to control the memory vs processing power trade-off.
   */
  cacheGzippedContent("cacheGzippedContent", Boolean.FALSE),
  
  /**
   * Whether the cache headers should be set or not, in case they are managed outside of WRO. By default, HTTP cache is
   * enabled.
   */
  cacheHttpEnabled("cache.http.enabled", Boolean.TRUE),
  
  /**
   * Parameter to set the value of the HTTP cache header.
   */
  cacheHttpValue("cache.http.value", "public, max-age=315360000"),
  
  /**
   * Parameter containing an integer value for specifying how often (in seconds) the cache should be refreshed.
   */
  cacheUpdatePeriod("cacheUpdatePeriod", 0L),
  
  /**
   * After how many seconds the connection to servlet context and external url will be timed-out. This is useful to
   * avoid memory leaks when connection pool responsible for cache and model reload is destroyed.
   */
  connectionTimeout("connectionTimeout", 2000),
  
  /**
   * If true, we are running in DEVELOPMENT mode. By default this value is true.
   */
  debug("debug", Boolean.TRUE),
  
  /**
   * The deployment mode. This value is set to PRODUCTION by default to ensure the stricter mode is enforced.
   */
  deploymentMode("deployment.mode", DeploymentMode.DEPLOYMENT),
  
  /**
   * Disable cache configuration option. When true, the processed content won't be cached in DEVELOPMENT mode. In
   * DEPLOYMENT mode changing this flag will have no effect.
   */
  disableCache("disableCache", Boolean.FALSE),
  
  /**
   * Encoding to use when reading and writing bytes from/to stream
   */
  encoding("encoding", StandardCharsets.UTF_8.name()),
  
  /**
   * Boolean flag for enable/disable resource gzipping.
   */
  gzipResources("gzipResources", Boolean.TRUE),
  
  /**
   * The parameter used to specify headers to put into the response, used mainly for caching.
   */
  header("header", null),
  
  /**
   * When a group is empty and this flag is false, the processing will fail. This is useful for runtime solution to
   * allow filter chaining when there is nothing to process for a given request.
   */
  ignoreEmptyGroup("ignoreEmptyGroup", Boolean.TRUE),
  
  /**
   * When this flag is true, any failure during processor will leave the content unchanged. Otherwise, the exception
   * will interrupt processing with a {@link RuntimeException}.
   */
  ignoreFailingProcessor("ignoreFailingProcessor", Boolean.FALSE),
  
  /**
   * Instructs wro4j to not throw an exception when a resource is missing.
   */
  ignoreMissingResources("ignoreMissingResources", Boolean.TRUE),
  
  /**
   * Parameter allowing to turn jmx on or off.
   */
  jmxEnabled("jmxEnabled", Boolean.TRUE),
  
  /**
   * The fully qualified class name of the {@link WroManagerFactory} implementation.
   */
  managerFactoryClassName("managerFactoryClassName", null),
  
  /**
   * the name of MBean to be used by JMX to configure wro4j.
   */
  mbeanName("mbeanName", null),
  
  /**
   * Flag indicating if the minimization is enabled. When this flag is false, the minimization will be suppressed for
   * all resources.
   */
  minimizeEnabled("minimizeEnabled", Boolean.TRUE),
  
  /**
   * Parameter containing an integer value for specifying how often (in seconds) the model should be refreshed.
   */
  modelUpdatePeriod("modelUpdatePeriod", 0L),
  
  /**
   * When true, will run in parallel pre processing of multiple resources. In theory this should improve the
   * performance.
   */
  parallelPreprocessing("parallelPreprocessing", Boolean.FALSE),
  
  /**
   * Flag which enables an experimental feature: asynchronous check for resource watcher.
   */
  resourceWatcherAsync("resourceWatcherAsync", Boolean.FALSE),
  
  /**
   * Parameter containing an integer value for specifying how often (in seconds) to run a thread responsible for
   * checking resource changes. When a change is detected, the cache for that particular group is invalidated.
   */
  resourceWatcherUpdatePeriod("resourceWatcherUpdatePeriod", 0L);
  
  private String propertyKey;
  private Object propertyValue;
  
  /**
   * Constructor defining the property key to use for each configuration option. This allows for more structured
   * configuration properties.
   * 
   * @param propertyKey
   *          The property's key.
   * @param defaultValue
   *          The default property value.
   */
  private ConfigConstants(String propertyKey, Object defaultValue) {
    this.propertyKey = propertyKey;
    propertyValue = defaultValue;
  }
  
  public String getPropertyKey() {
    return propertyKey;
  }
  
  public Object getDefaultPropertyValue() {
    return propertyValue;
  }
  
}
