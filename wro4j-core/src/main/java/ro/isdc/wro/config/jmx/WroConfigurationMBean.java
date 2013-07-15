/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.config.jmx;

/**
 * This interface defines the MBean which manage the wro4j configuration.
 * 
 * @author Alex Objelean
 */
public interface WroConfigurationMBean {
  /**
   * @return period in seconds when the wro model (by default read from wro.xml) is updated.
   */
  long getModelUpdatePeriod();
  
  /**
   * Set after how many seconds the wro model should be updated.
   * 
   * @param period
   *          number of seconds. Zero or less, means never.
   */
  void setModelUpdatePeriod(final long period);
  
  /**
   * @return period in seconds when the cache containing merged resources by group should be updated.
   */
  long getCacheUpdatePeriod();
  
  /**
   * Set after how many seconds the cache containing merged resources by group name should be updated.
   * 
   * @param period
   *          number of seconds. Zero or less, means never.
   */
  void setCacheUpdatePeriod(final long period);
  
  /**
   * @return period in seconds indicating how often resource changes are checked.
   */
  long getResourceWatcherUpdatePeriod();
  
  /**
   * Set after how many seconds the resource change detection is performed..
   * 
   * @param period
   *          number of seconds. Zero or less, means never.
   */
  void setResourceWatcherUpdatePeriod(final long period);
  
  /**
   * @return true if Gzip is Enabled.
   */
  boolean isGzipEnabled();
  
  /**
   * @param enable
   *          if true, gzip will be enabled, otherwise will be disabled.
   */
  void setGzipEnabled(final boolean enable);
  
  /**
   * @return the state of debug flag.
   */
  boolean isDebug();
  
  /**
   * Set the debug mode.
   * 
   * @param debug
   *          if true, the wro4j will run in DEVELOPMENT MODE.
   */
  void setDebug(final boolean debug);
  
  /**
   * Force reload of the cache.
   */
  void reloadCache();
  
  /**
   * Force reload of the model.
   */
  void reloadModel();
  
  /**
   * After how many seconds the connection to servlet context and external url will be timed-out. This is useful to
   * avoid memory leaks when connection pool responsible for cache and model reload is destroyed.
   * 
   * @param seconds
   *          value in seconds.
   */
  void setConnectionTimeout(int seconds);
  
  /**
   * @return the number of seconds to wait until connection will timeout.
   */
  int getConnectionTimeout();
  
  /**
   * Whether gzipped content should be cached or not. In order to take effect, after changing this value, reload the
   * cache.
   * 
   * @param cache
   *          boolean flag.
   */
  void setCacheGzippedContent(boolean cache);
  
  /**
   * @return true if the cacheGzippedContent flag is enabled.
   */
  boolean isCacheGzippedContent();
  
  /**
   * Encoding to use when writing the processed result into the output stream.
   * 
   * @param encoding
   *          name of the charset encoding to set.
   */
  void setEncoding(String encoding);
  
  /**
   * @return currently used encoding.
   */
  String getEncoding();
  
  /**
   * @return if missing resources should be ignored.
   */
  boolean isIgnoreMissingResources();
  
  /**
   * @return set the ignoreMissingResources flag.
   */
  void setIgnoreMissingResources(boolean ignore);
  
  /**
   * @param ignore
   *          turns on/off failure on empty group (when there is nothing to process).
   */
  void setIgnoreEmptyGroup(boolean ignore);
  
  /**
   * @return value of the flag responsible for handling empty group behavior.
   */
  boolean isIgnoreEmptyGroup();
  
  /**
   * @param minifyResources
   *          set to false to override minification settings on all resources
   */
  void setMinifyResources(boolean minifyResources);
  
  /**
   * @return value of flag that controls override of resource minification
   */
  boolean isMinifyResources();
}
