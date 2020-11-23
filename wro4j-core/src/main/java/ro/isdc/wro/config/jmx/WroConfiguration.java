/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.config.jmx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.config.support.DeploymentMode;
import ro.isdc.wro.manager.factory.WroManagerFactory;

/**
 * Defines MBean which manage configuration. There should be only one instance
 * of this object in the application and it should be accessible even outside of
 * the request cycle.
 *
 * @author Alex Objelean
 * @author Paul Podgorsek
 */
public class WroConfiguration implements WroConfigurationMBean {

	private static final Logger LOG = LoggerFactory.getLogger(WroConfiguration.class);

	/**
	 * When this flag is enabled, the raw processed content will be gzipped only the
	 * first time and all subsequent requests will use the cached gzipped content.
	 * Otherwise, the gzip operation will be performed for each request. This flag
	 * allow to control the memory vs processing power trade-off.
	 */
	private boolean cacheGzippedContent = (Boolean) ConfigConstants.cacheGzippedContent.getDefaultPropertyValue();

	/**
	 * Whether the cache HTTP headers should be set or not, in case they are managed
	 * outside of WRO. By default, HTTP cache is enabled.
	 */
	private boolean cacheHttpEnabled = (Boolean) ConfigConstants.cacheHttpEnabled.getDefaultPropertyValue();

	private String cacheHttpValue = (String) ConfigConstants.cacheHttpValue.getDefaultPropertyValue();

	/**
	 * How often to run a thread responsible for refreshing the cache.
	 */
	private long cacheUpdatePeriod = (Long) ConfigConstants.cacheUpdatePeriod.getDefaultPropertyValue();

	/**
	 * Timeout (milliseconds) of the url connection for external resources. This is
	 * used to ensure that locator doesn't spend too much time on slow end-point.
	 */
	private int connectionTimeout = (Integer) ConfigConstants.connectionTimeout.getDefaultPropertyValue();

	/**
	 * If true, we are running in DEVELOPMENT mode.
	 */
	private boolean debug = (Boolean) ConfigConstants.debug.getDefaultPropertyValue();

	private DeploymentMode deploymentMode = (DeploymentMode) ConfigConstants.deploymentMode.getDefaultPropertyValue();

	/**
	 * Encoding to use when reading resources.
	 */
	private String encoding = (String) ConfigConstants.encoding.getDefaultPropertyValue();

	/**
	 * Gzip enable flag.
	 */
	private boolean gzipEnabled = (Boolean) ConfigConstants.gzipResources.getDefaultPropertyValue();

	/**
	 * The parameter used to specify headers to put into the response, used mainly
	 * for caching.
	 */
	private String header = (String) ConfigConstants.header.getDefaultPropertyValue();

	/**
	 * When a group is empty and this flag is false, the processing will fail. This
	 * is useful for runtime solution to allow filter chaining when there is nothing
	 * to process for a given request.
	 */
	private boolean ignoreEmptyGroup = (Boolean) ConfigConstants.ignoreEmptyGroup.getDefaultPropertyValue();

	/**
	 * When this flag is true, any failure during processor, will leave the content
	 * unchanged. Otherwise, the exception will interrupt processing with a
	 * {@link RuntimeException}.
	 */
	private boolean ignoreFailingProcessor = (Boolean) ConfigConstants.ignoreFailingProcessor.getDefaultPropertyValue();

	/**
	 * If true, missing resources are ignored.
	 */
	private boolean ignoreMissingResources = (Boolean) ConfigConstants.ignoreMissingResources.getDefaultPropertyValue();

	/**
	 * Allow to turn jmx on or off. By default this value is true.
	 */
	private boolean jmxEnabled = (Boolean) ConfigConstants.jmxEnabled.getDefaultPropertyValue();

	/**
	 * A preferred name of the MBean object.
	 */
	private String mbeanName = (String) ConfigConstants.mbeanName.getDefaultPropertyValue();

	/**
	 * When this flag is false, the minimization will be suppressed for all
	 * resources. This flag is enabled by default.
	 */
	private boolean minimizeEnabled = (Boolean) ConfigConstants.minimizeEnabled.getDefaultPropertyValue();

	/**
	 * How often to run a thread responsible for refreshing the model.
	 */
	private long modelUpdatePeriod = (Long) ConfigConstants.modelUpdatePeriod.getDefaultPropertyValue();

	/**
	 * When true, will run in parallel preprocessing of multiple resources. In
	 * theory this should improve the performance. By default this flag is false,
	 * because this feature is experimental.
	 */
	private boolean parallelPreprocessing = (Boolean) ConfigConstants.parallelPreprocessing.getDefaultPropertyValue();

	/**
	 * How often to run a thread responsible for detecting resource changes.
	 */
	private long resourceWatcherUpdatePeriod = (Long) ConfigConstants.resourceWatcherUpdatePeriod
			.getDefaultPropertyValue();

	/**
	 * Flag for enabling an experimental feature which allows asynchronous resource
	 * watcher check.
	 */
	private boolean resourceWatcherAsync = (Boolean) ConfigConstants.resourceWatcherAsync.getDefaultPropertyValue();

	/**
	 * Fully qualified class name of the {@link WroManagerFactory} implementation.
	 */
	private String wroManagerClassName = (String) ConfigConstants.managerFactoryClassName.getDefaultPropertyValue();
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
	 * Perform the cleanup, clear the listeners.
	 */
	public void destroy() {
		cacheUpdatePeriodListeners.clear();
		modelUpdatePeriodListeners.clear();
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, true);
	}

	public String getCacheHttpValue() {
		return cacheHttpValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCacheUpdatePeriod() {
		return cacheUpdatePeriod;
	}

	/**
	 * @return the number of milliseconds before a connection is timed out.
	 */
	@Override
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public DeploymentMode getDeploymentMode() {
		return deploymentMode;
	}

	/**
	 * @return the encoding
	 */
	@Override
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @return the mbeanName
	 */
	public String getMbeanName() {
		return mbeanName;
	}

	/**
	 * {@inheritDoc}Async
	 */
	@Override
	public long getModelUpdatePeriod() {
		return modelUpdatePeriod;
	}

	@Override
	public final long getResourceWatcherUpdatePeriod() {
		return resourceWatcherUpdatePeriod;
	}

	/**
	 * @return the wroManagerClassName
	 */
	public String getWroManagerClassName() {
		return wroManagerClassName;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * @return the cacheGzippedContent
	 */
	@Override
	public boolean isCacheGzippedContent() {
		return cacheGzippedContent;
	}

	public boolean isCacheHttpEnabled() {
		return cacheHttpEnabled;
	}

	/**
	 * @return the debug
	 */
	@Override
	public boolean isDebug() {
		return debug;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGzipEnabled() {
		return gzipEnabled;
	}

	/**
	 * @return value of the flag responsible for handling empty group behavior.
	 */
	@Override
	public boolean isIgnoreEmptyGroup() {
		return ignoreEmptyGroup;
	}

	/**
	 * @return true if the processing failure should be ignored.
	 */
	public boolean isIgnoreFailingProcessor() {
		return ignoreFailingProcessor;
	}

	/**
	 * @return the ignoreMissingResources
	 */
	@Override
	public boolean isIgnoreMissingResources() {
		return ignoreMissingResources;
	}

	/**
	 * @return the jmxEnabled
	 */
	public boolean isJmxEnabled() {
		return jmxEnabled;
	}

	/**
	 * @return flag indicating if the minimization is enabled. When this flag is
	 *         false, the minimization will be suppressed for all resources.
	 */
	@Override
	public boolean isMinimizeEnabled() {
		return minimizeEnabled;
	}

	/**
	 * @return the parallelPreprocessing
	 */
	public boolean isParallelPreprocessing() {
		return parallelPreprocessing;
	}

	/**
	 * @return true if the asynchronous resourceWatcher experimental feature is
	 *         enabled.
	 */
	public boolean isResourceWatcherAsync() {
		return resourceWatcherAsync;
	}

	/**
	 * Register a listener which is notified when the modelUpdate period value is
	 * changed.
	 *
	 * @param listener to add.
	 */
	public void registerCacheUpdatePeriodChangeListener(final PropertyChangeListener listener) {
		cacheUpdatePeriodListeners.add(listener);
	}

	/**
	 * Register a listener which is notified when the modelUpdate period value is
	 * changed. Registration is allowed only during
	 *
	 * @param listener to add.
	 */
	public void registerModelUpdatePeriodChangeListener(final PropertyChangeListener listener) {
		modelUpdatePeriodListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reloadCache() {
		reloadCacheWithNewValue(null);
	}

	/**
	 * Notify all listeners about cachePeriod property changed. If passed newValue
	 * is null, the oldValue is taken as new value. This is the case when the
	 * reloadCache is invoked.
	 *
	 * @param newValue value to set.
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
	@Override
	public void reloadModel() {
		LOG.debug("reloadModel");
		reloadModelWithNewValue(null);
	}

	/**
	 * Notify all listeners about cachePeriod property changed. If passed newValue
	 * is null, the oldValue is taken as new value. This is the case when the
	 * reloadModel is invoked.
	 *
	 * @param newValue value to set.
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
	 * @param cacheGzippedContent the cacheGzippedContent to set
	 */
	@Override
	public void setCacheGzippedContent(final boolean cacheGzippedContent) {
		this.cacheGzippedContent = cacheGzippedContent;
	}

	/**
	 * @param cacheGzippedContent the cacheGzippedContent to set
	 */
	public void setCacheGzippedContent(final Boolean cacheGzippedContent) {
		if (cacheGzippedContent != null) {
			setCacheGzippedContent(cacheGzippedContent.booleanValue());
		}
	}

	public void setCacheHttpEnabled(final boolean cacheHttpEnabled) {
		this.cacheHttpEnabled = cacheHttpEnabled;
	}

	public void setCacheHttpEnabled(final Boolean cacheHttpEnabled) {
		if (cacheHttpEnabled != null) {
			setCacheHttpEnabled(cacheHttpEnabled.booleanValue());
		}
	}

	public void setCacheHttpValue(final String cacheHttpValue) {
		if (cacheHttpValue != null) {
			this.cacheHttpValue = cacheHttpValue;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCacheUpdatePeriod(final long period) {
		if (period != cacheUpdatePeriod) {
			reloadCacheWithNewValue(period);
		}
		this.cacheUpdatePeriod = period;
	}

	public void setCacheUpdatePeriod(final Long period) {
		if (period != null) {
			setCacheUpdatePeriod((long) period);
		}
	}

	/**
	 * Timeout (milliseconds) of the url connection for external resources. This is
	 * used to ensure that locator doesn't spend too much time on slow end-point.
	 *
	 * @param connectionTimeout the connectionTimeout to set
	 */
	@Override
	public void setConnectionTimeout(final int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setConnectionTimeout(final Integer connectionTimeout) {
		if (connectionTimeout != null) {
			setConnectionTimeout((int) connectionTimeout);
		}
	}

	/**
	 * @param debug the debug to set
	 */
	@Override
	public void setDebug(final boolean debug) {
		// Don't think that we really need to reload the cache here
		this.debug = debug;
	}

	public void setDebug(final Boolean debug) {
		if (debug != null) {
			setDebug(debug.booleanValue());
		}
	}

	public void setDeploymentMode(DeploymentMode deploymentMode) {
		if (deploymentMode != null) {
			this.deploymentMode = deploymentMode;
		}
	}

	/**
	 * Sets the encoding. If the encoding is {@code null}, this method won't do
	 * anything, in order to rely on the default value.
	 * 
	 * @param encoding the encoding to set.
	 */
	@Override
	public void setEncoding(final String encoding) {
		if (encoding != null) {
			this.encoding = encoding;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGzipEnabled(final boolean enabled) {
		gzipEnabled = enabled;
	}

	public void setGzipEnabled(final Boolean enabled) {
		if (enabled != null) {
			setGzipEnabled(enabled.booleanValue());
		}
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(final String header) {
		if (header != null) {
			this.header = header;
		}
	}

	/**
	 * @param ignoreEmptyGroup flag for turning on/off failure when there is an
	 *                         empty group (nothing to process). This value is true
	 *                         by default, meaning that empty group will produce
	 *                         empty result (no exception).
	 */
	@Override
	public void setIgnoreEmptyGroup(final boolean ignoreEmptyGroup) {
		this.ignoreEmptyGroup = ignoreEmptyGroup;
	}

	public void setIgnoreEmptyGroup(final Boolean ignoreEmptyGroup) {
		if (ignoreEmptyGroup != null) {
			setIgnoreEmptyGroup(ignoreEmptyGroup.booleanValue());
		}
	}

	public void setIgnoreFailingProcessor(final boolean ignoreFailingProcessor) {
		this.ignoreFailingProcessor = ignoreFailingProcessor;
	}

	public void setIgnoreFailingProcessor(final Boolean ignoreFailingProcessor) {
		if (ignoreFailingProcessor != null) {
			setIgnoreFailingProcessor(ignoreFailingProcessor.booleanValue());
		}
	}

	/**
	 * @param ignoreMissingResources the ignoreMissingResources to set
	 */
	@Override
	public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
		this.ignoreMissingResources = ignoreMissingResources;
	}

	public void setIgnoreMissingResources(final Boolean ignoreMissingResources) {
		if (ignoreMissingResources != null) {
			setIgnoreMissingResources(ignoreMissingResources.booleanValue());
		}
	}

	/**
	 * @param jmxEnabled the jmxEnabled to set
	 */
	public void setJmxEnabled(final boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	public void setJmxEnabled(final Boolean jmxEnabled) {
		if (jmxEnabled != null) {
			setJmxEnabled(jmxEnabled.booleanValue());
		}
	}

	/**
	 * @param mbeanName the mbeanName to set
	 */
	public void setMbeanName(final String mbeanName) {
		if (mbeanName != null) {
			this.mbeanName = mbeanName;
		}
	}

	@Override
	public void setMinimizeEnabled(final boolean minimizeEnabled) {
		this.minimizeEnabled = minimizeEnabled;
	}

	public void setMinimizeEnabled(final Boolean minimizeEnabled) {
		if (minimizeEnabled != null) {
			setMinimizeEnabled(minimizeEnabled.booleanValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setModelUpdatePeriod(final long period) {
		if (period != modelUpdatePeriod) {
			reloadModelWithNewValue(period);
		}
		this.modelUpdatePeriod = period;
	}

	public void setModelUpdatePeriod(final Long period) {
		if (period != null) {
			setModelUpdatePeriod((long) period);
		}
	}

	/**
	 * @param parallelPreprocessing the parallelPreprocessing to set
	 */
	public void setParallelPreprocessing(final boolean parallelPreprocessing) {
		this.parallelPreprocessing = parallelPreprocessing;
	}

	public void setParallelPreprocessing(final Boolean parallelPreprocessing) {
		if (parallelPreprocessing != null) {
			setParallelPreprocessing(parallelPreprocessing.booleanValue());
		}
	}

	public void setResourceWatcherAsync(final boolean resourceWatcherAsync) {
		this.resourceWatcherAsync = resourceWatcherAsync;
	}

	public void setResourceWatcherAsync(final Boolean resourceWatcherAsync) {
		if (resourceWatcherAsync != null) {
			setResourceWatcherAsync(resourceWatcherAsync.booleanValue());
		}
	}

	@Override
	public final void setResourceWatcherUpdatePeriod(final long period) {
		this.resourceWatcherUpdatePeriod = period;
	}

	public void setResourceWatcherUpdatePeriod(final Long period) {
		if (period != null) {
			setResourceWatcherUpdatePeriod((long) period);
		}
	}

	/**
	 * @param wroManagerClassName the wroManagerClassName to set
	 */
	public void setWroManagerClassName(final String wroManagerClassName) {
		if (wroManagerClassName != null) {
			this.wroManagerClassName = wroManagerClassName;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE).toString();
	}

}
