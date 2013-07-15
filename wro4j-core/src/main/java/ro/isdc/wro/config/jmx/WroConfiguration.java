/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.config.jmx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * Defines MBean which manage configuration. There should be only one instance of this object in the application and it
 * should be accessible even outside of the request cycle.
 * 
 * @author Alex Objelean
 */
public final class WroConfiguration
    implements WroConfigurationMBean {
  private static final Logger LOG = LoggerFactory.getLogger(WroConfiguration.class);
  /**
   * Default encoding to use.
   */
  public static final String DEFAULT_ENCODING = CharEncoding.UTF_8;
  /**
   * Default value for connectionTimeout property.
   */
  public static int DEFAULT_CONNECTION_TIMEOUT = 2000;
  /**
   * How often to run a thread responsible for refreshing the cache.
   */
  private long cacheUpdatePeriod;
  /**
   * How often to run a thread responsible for refreshing the model.
   */
  private long modelUpdatePeriod;
  /**
   * How often to run a thread responsible for detecting resource changes.
   */
  private long resourceWatcherUpdatePeriod;
  /**
   * Gzip enable flag.
   */
  private boolean gzipEnabled = true;
  /**
   * If true, we are running in DEVELOPMENT mode. By default this value is true.
   */
  private boolean debug = true;
  /**
   * If true, missing resources are ignored. By default this value is true.
   */
  private boolean ignoreMissingResources = true;
  /**
   * Flag which will force no caching of the processed content only in DEVELOPMENT mode. In DEPLOYMENT mode changing
   * this flag will have no effect. By default this value is false.
   */
  private boolean disableCache = false;
  
  /**
   * When this flag is enabled, the raw processed content will be gzipped only the first time and all subsequent
   * requests will use the cached gzipped content. Otherwise, the gzip operation will be performed for each request.
   * This flag allow to control the memory vs processing power trade-off.
   */
  private boolean cacheGzippedContent = false;
  /**
   * Allow to turn jmx on or off. By default this value is true.
   */
  private boolean jmxEnabled = true;
  /**
   * Fully qualified class name of the {@link WroManagerFactory} implementation.
   */
  private String wroManagerClassName;
  /**
   * Encoding to use when reading resources.
   */
  private String encoding = DEFAULT_ENCODING;
  /**
   * A preferred name of the MBean object.
   */
  private String mbeanName;
  /**
   * The parameter used to specify headers to put into the response, used mainly for caching.
   */
  private String header;
  /**
   * Timeout (milliseconds) of the url connection for external resources. This is used to ensure that locator doesn't
   * spend too much time on slow end-point.
   */
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
  /**
   * When true, will run in parallel preprocessing of multiple resources. In theory this should improve the performance.
   * By default this flag is false, because this feature is experimental.
   */
  private boolean parallelPreprocessing = false;
  /**
   * When a group is empty and this flag is false, the processing will fail. This is useful for runtime solution to
   * allow filter chaining when there is nothing to process for a given request.
   */
  private boolean ignoreEmptyGroup = true;
  /**
   * When this flag is true, any failure during processor, will leave the content unchanged. Otherwise, the exception
   * will interrupt processing with a {@link RuntimeException}.
   */
  private boolean ignoreFailingProcessor = false;
  /**
   * When this flag is false, minification will be turned off on all resources
   */
  private boolean minifyResources = true;
  /**
   * Listeners for the change of cache & model period properties.
   */
  private final transient List<PropertyChangeListener> cacheUpdatePeriodListeners = new ArrayList<PropertyChangeListener>(
      1);
  private final transient List<PropertyChangeListener> modelUpdatePeriodListeners = new ArrayList<PropertyChangeListener>(
      1);
  
  /**
   * @return the name of the object used to register the MBean.
   */
  public static String getObjectName() {
    return WroConfiguration.class.getPackage().getName() + ".jmx:type=" + WroConfiguration.class.getSimpleName();
  }
  
  /**
   * {@inheritDoc}
   */
  public long getCacheUpdatePeriod() {
    return this.cacheUpdatePeriod;
  }
  
  /**
   * {@inheritDoc}
   */
  public long getModelUpdatePeriod() {
    return modelUpdatePeriod;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setCacheUpdatePeriod(final long period) {
    if (period != cacheUpdatePeriod) {
      reloadCacheWithNewValue(period);
    }
    this.cacheUpdatePeriod = period;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setModelUpdatePeriod(final long period) {
    if (period != modelUpdatePeriod) {
      reloadModelWithNewValue(period);
    }
    this.modelUpdatePeriod = period;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isGzipEnabled() {
    return gzipEnabled;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setGzipEnabled(final boolean enable) {
    gzipEnabled = enable;
  }
  
  /**
   * {@inheritDoc}
   */
  public void reloadCache() {
    reloadCacheWithNewValue(null);
  }
  
  /**
   * Notify all listeners about cachePeriod property changed. If passed newValue is null, the oldValue is taken as new
   * value. This is the case when the reloadCache is invoked.
   * 
   * @param newValue
   *          value to set.
   */
  private void reloadCacheWithNewValue(final Long newValue) {
    final long newValueAsPrimitive = newValue == null ? getCacheUpdatePeriod() : newValue;
    LOG.debug("invoking {} listeners", cacheUpdatePeriodListeners.size());
    for (final PropertyChangeListener listener : cacheUpdatePeriodListeners) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this, "cache", getCacheUpdatePeriod(),
          newValueAsPrimitive);
      listener.propertyChange(event);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void reloadModel() {
    LOG.debug("reloadModel");
    reloadModelWithNewValue(null);
  }
  
  /**
   * Notify all listeners about cachePeriod property changed. If passed newValue is null, the oldValue is taken as new
   * value. This is the case when the reloadModel is invoked.
   * 
   * @param newValue
   *          value to set.
   */
  private void reloadModelWithNewValue(final Long newValue) {
    final long newValueAsPrimitive = newValue == null ? getModelUpdatePeriod() : newValue;
    for (final PropertyChangeListener listener : modelUpdatePeriodListeners) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this, "model", getModelUpdatePeriod(),
          newValueAsPrimitive);
      listener.propertyChange(event);
    }
  }
  
  /**
   * Register a listener which is notified when the modelUpdate period value is changed. Registration is allowed only
   * during
   * 
   * @param listener
   *          to add.
   */
  public void registerModelUpdatePeriodChangeListener(final PropertyChangeListener listener) {
    modelUpdatePeriodListeners.add(listener);
  }
  
  /**
   * Register a listener which is notified when the modelUpdate period value is changed.
   * 
   * @param listener
   *          to add.
   */
  public void registerCacheUpdatePeriodChangeListener(final PropertyChangeListener listener) {
    cacheUpdatePeriodListeners.add(listener);
  }
  
  /**
   * @return the debug
   */
  public boolean isDebug() {
    return this.debug;
  }
  
  /**
   * @param debug
   *          the debug to set
   */
  public void setDebug(final boolean debug) {
    // Don't think that we really need to reload the cache here
    this.debug = debug;
  }
  
  /**
   * @return the ignoreMissingResources
   */
  public boolean isIgnoreMissingResources() {
    return this.ignoreMissingResources;
  }
  
  /**
   * @param ignoreMissingResources
   *          the ignoreMissingResources to set
   */
  public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    this.ignoreMissingResources = ignoreMissingResources;
  }
  
  /**
   * @return the disableCache
   */
  public boolean isDisableCache() {
    // disable cache only if you are in DEVELOPMENT mode (aka debug)
    return this.disableCache && debug;
  }
  
  /**
   * @param disableCache
   *          the disableCache to set
   */
  public void setDisableCache(final boolean disableCache) {
    if (!debug && disableCache) {
      LOG.warn("You cannot disable cache in DEPLOYMENT mode");
    }
    this.disableCache = disableCache;
  }
  
  /**
   * @return the jmxEnabled
   */
  public boolean isJmxEnabled() {
    return jmxEnabled;
  }
  
  /**
   * @param jmxEnabled
   *          the jmxEnabled to set
   */
  public void setJmxEnabled(final boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
  }
  
  /**
   * @return the cacheGzippedContent
   */
  public boolean isCacheGzippedContent() {
    return this.cacheGzippedContent;
  }
  
  /**
   * @param cacheGzippedContent
   *          the cacheGzippedContent to set
   */
  public void setCacheGzippedContent(final boolean cacheGzippedContent) {
    this.cacheGzippedContent = cacheGzippedContent;
  }
  
  /**
   * Perform the cleanup, clear the listeners.
   */
  public void destroy() {
    cacheUpdatePeriodListeners.clear();
    modelUpdatePeriodListeners.clear();
  }
  
  /**
   * @return the encoding
   */
  public String getEncoding() {
    return this.encoding;
  }
  
  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding(final String encoding) {
    this.encoding = encoding == null ? DEFAULT_ENCODING : encoding;
  }
  
  /**
   * @return the wroManagerClassName
   */
  public String getWroManagerClassName() {
    return this.wroManagerClassName;
  }
  
  /**
   * @param wroManagerClassName
   *          the wroManagerClassName to set
   */
  public void setWroManagerClassName(final String wroManagerClassName) {
    this.wroManagerClassName = wroManagerClassName;
  }
  
  /**
   * @return the mbeanName
   */
  public String getMbeanName() {
    return this.mbeanName;
  }
  
  /**
   * @param mbeanName
   *          the mbeanName to set
   */
  public void setMbeanName(final String mbeanName) {
    this.mbeanName = mbeanName;
  }
  
  /**
   * @return the header
   */
  public String getHeader() {
    return this.header;
  }
  
  /**
   * @param header
   *          the header to set
   */
  public void setHeader(final String header) {
    this.header = header;
  }
  
  /**
   * @return the number of milliseconds before a connection is timed out.
   */
  public int getConnectionTimeout() {
    return this.connectionTimeout;
  }
  
  /**
   * Timeout (milliseconds) of the url connection for external resources. This is used to ensure that locator doesn't
   * spend too much time on slow end-point.
   * 
   * @param connectionTimeout
   *          the connectionTimeout to set
   */
  public void setConnectionTimeout(final int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }
  
  /**
   * @return the parallelPreprocessing
   */
  public boolean isParallelPreprocessing() {
    return this.parallelPreprocessing;
  }
  
  /**
   * @param parallelPreprocessing
   *          the parallelPreprocessing to set
   */
  public void setParallelPreprocessing(final boolean parallelPreprocessing) {
    this.parallelPreprocessing = parallelPreprocessing;
  }
  
  /**
   * @return value of the flag responsible for handling empty group behavior.
   */
  public boolean isIgnoreEmptyGroup() {
    return ignoreEmptyGroup;
  }
  
  /**
   * @param ignoreEmptyGroup
   *          flag for turning on/off failure when there is an empty group (nothing to process). This value is true by
   *          default, meaning that empty group will produce empty result (no exception).
   */
  public void setIgnoreEmptyGroup(final boolean ignoreEmptyGroup) {
    this.ignoreEmptyGroup = ignoreEmptyGroup;
  }
  
  /**
   * @return true if the processing failure should be ignored.
   */
  public boolean isIgnoreFailingProcessor() {
    return ignoreFailingProcessor;
  }
  
  public void setIgnoreFailingProcessor(final boolean ignoreFailingProcessor) {
    this.ignoreFailingProcessor = ignoreFailingProcessor;
  }
  
  public final long getResourceWatcherUpdatePeriod() {
    return resourceWatcherUpdatePeriod;
  }
  
  public final void setResourceWatcherUpdatePeriod(final long resourceWatcherUpdatePeriod) {
    this.resourceWatcherUpdatePeriod = resourceWatcherUpdatePeriod;
  }
  
  public boolean isMinifyResources() {
    return minifyResources;
  }
  
  public void setMinifyResources(boolean minifyResources) {
    this.minifyResources = minifyResources;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, true);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }
}
