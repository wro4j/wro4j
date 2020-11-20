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
	 * Whether the cache HTTP headers should be set or not, in case they are managed
	 * outside of WRO. By default, HTTP cache is enabled.
	 */
	private Boolean cacheHttpEnabled;

	private String cacheHttpValue;

	/**
	 * How often to run a thread responsible for refreshing the cache.
	 */
	private Long cacheUpdatePeriod;

	private DeploymentMode deploymentMode;

	/**
	 * How often to run a thread responsible for refreshing the model.
	 */
	private Long modelUpdatePeriod;

	/**
	 * How often to run a thread responsible for detecting resource changes.
	 */
	private Long resourceWatcherUpdatePeriod;

	/**
	 * Flag for enabling an experimental feature which allows asynchronous resource
	 * watcher check.
	 */
	private Boolean resourceWatcherAsync;

	/**
	 * Gzip enable flag.
	 */
	private Boolean gzipEnabled;

	/**
	 * If true, we are running in DEVELOPMENT mode.
	 */
	private Boolean debug;

	/**
	 * If true, missing resources are ignored.
	 */
	private Boolean ignoreMissingResources;

	/**
	 * When this flag is enabled, the raw processed content will be gzipped only the
	 * first time and all subsequent requests will use the cached gzipped content.
	 * Otherwise, the gzip operation will be performed for each request. This flag
	 * allow to control the memory vs processing power trade-off.
	 */
	private Boolean cacheGzippedContent;

	/**
	 * Allow to turn jmx on or off. By default this value is true.
	 */
	private Boolean jmxEnabled;

	/**
	 * Fully qualified class name of the {@link WroManagerFactory} implementation.
	 */
	private String wroManagerClassName;

	/**
	 * Encoding to use when reading resources.
	 */
	private String encoding;

	/**
	 * A preferred name of the MBean object.
	 */
	private String mbeanName;

	/**
	 * The parameter used to specify headers to put into the response, used mainly
	 * for caching.
	 */
	private String header;

	/**
	 * Timeout (milliseconds) of the url connection for external resources. This is
	 * used to ensure that locator doesn't spend too much time on slow end-point.
	 */
	private Integer connectionTimeout;

	/**
	 * When true, will run in parallel preprocessing of multiple resources. In
	 * theory this should improve the performance. By default this flag is false,
	 * because this feature is experimental.
	 */
	private Boolean parallelPreprocessing;

	/**
	 * When a group is empty and this flag is false, the processing will fail. This
	 * is useful for runtime solution to allow filter chaining when there is nothing
	 * to process for a given request.
	 */
	private Boolean ignoreEmptyGroup;

	/**
	 * When this flag is true, any failure during processor, will leave the content
	 * unchanged. Otherwise, the exception will interrupt processing with a
	 * {@link RuntimeException}.
	 */
	private Boolean ignoreFailingProcessor;

	/**
	 * When this flag is false, the minimization will be suppressed for all
	 * resources. This flag is enabled by default.
	 */
	private Boolean minimizeEnabled;

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
	@Override
	public long getCacheUpdatePeriod() {

		if (cacheUpdatePeriod == null) {
			return (Long) ConfigConstants.cacheUpdatePeriod.getDefaultPropertyValue();
		} else {
			return cacheUpdatePeriod;
		}
	}

	/**
	 * {@inheritDoc}Async
	 */
	@Override
	public long getModelUpdatePeriod() {

		if (modelUpdatePeriod == null) {
			return (Long) ConfigConstants.modelUpdatePeriod.getDefaultPropertyValue();
		} else {
			return modelUpdatePeriod;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCacheUpdatePeriod(final Long period) {
		if (period != cacheUpdatePeriod) {
			reloadCacheWithNewValue(period);
		}
		this.cacheUpdatePeriod = period;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setModelUpdatePeriod(final Long period) {
		if (period != modelUpdatePeriod) {
			reloadModelWithNewValue(period);
		}
		this.modelUpdatePeriod = period;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGzipEnabled() {

		if (gzipEnabled == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.gzipResources.getDefaultPropertyValue());
		} else {
			return gzipEnabled;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGzipEnabled(final Boolean enable) {
		gzipEnabled = enable;
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
	 * Register a listener which is notified when the modelUpdate period value is
	 * changed. Registration is allowed only during
	 *
	 * @param listener to add.
	 */
	public void registerModelUpdatePeriodChangeListener(final PropertyChangeListener listener) {
		modelUpdatePeriodListeners.add(listener);
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
	 * @return the debug
	 */
	@Override
	public boolean isDebug() {

		if (debug == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.debug.getDefaultPropertyValue());
		} else {
			return debug;
		}
	}

	/**
	 * @param debug the debug to set
	 */
	@Override
	public void setDebug(final Boolean debug) {
		// Don't think that we really need to reload the cache here
		this.debug = debug;
	}

	/**
	 * @return the ignoreMissingResources
	 */
	@Override
	public boolean isIgnoreMissingResources() {

		if (ignoreMissingResources == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.ignoreMissingResources.getDefaultPropertyValue());
		} else {
			return ignoreMissingResources;
		}
	}

	/**
	 * @param ignoreMissingResources the ignoreMissingResources to set
	 */
	@Override
	public void setIgnoreMissingResources(final Boolean ignoreMissingResources) {
		this.ignoreMissingResources = ignoreMissingResources;
	}

	/**
	 * @return the jmxEnabled
	 */
	public boolean isJmxEnabled() {

		if (jmxEnabled == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.jmxEnabled.getDefaultPropertyValue());
		} else {
			return jmxEnabled;
		}
	}

	/**
	 * @param jmxEnabled the jmxEnabled to set
	 */
	public void setJmxEnabled(final Boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	/**
	 * @return the cacheGzippedContent
	 */
	@Override
	public boolean isCacheGzippedContent() {

		if (cacheGzippedContent == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.cacheGzippedContent.getDefaultPropertyValue());
		} else {
			return cacheGzippedContent;
		}
	}

	/**
	 * @param cacheGzippedContent the cacheGzippedContent to set
	 */
	@Override
	public void setCacheGzippedContent(final Boolean cacheGzippedContent) {
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
	@Override
	public String getEncoding() {

		if (encoding == null) {
			return (String) ConfigConstants.encoding.getDefaultPropertyValue();
		} else {
			return encoding;
		}
	}

	/**
	 * @param encoding the encoding to set
	 */
	@Override
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	/**
	 * @return the wroManagerClassName
	 */
	public String getWroManagerClassName() {

		if (wroManagerClassName == null) {
			return (String) ConfigConstants.managerFactoryClassName.getDefaultPropertyValue();
		} else {
			return wroManagerClassName;
		}
	}

	/**
	 * @param wroManagerClassName the wroManagerClassName to set
	 */
	public void setWroManagerClassName(final String wroManagerClassName) {
		this.wroManagerClassName = wroManagerClassName;
	}

	/**
	 * @return the mbeanName
	 */
	public String getMbeanName() {

		if (mbeanName == null) {
			return (String) ConfigConstants.mbeanName.getDefaultPropertyValue();
		} else {
			return mbeanName;
		}
	}

	/**
	 * @param mbeanName the mbeanName to set
	 */
	public void setMbeanName(final String mbeanName) {
		this.mbeanName = mbeanName;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {

		if (header == null) {
			return (String) ConfigConstants.header.getDefaultPropertyValue();
		} else {
			return header;
		}
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(final String header) {
		this.header = header;
	}

	/**
	 * @return the number of milliseconds before a connection is timed out.
	 */
	@Override
	public int getConnectionTimeout() {

		if (connectionTimeout == null) {
			return (Integer) ConfigConstants.connectionTimeout.getDefaultPropertyValue();
		} else {
			return connectionTimeout;
		}
	}

	/**
	 * Timeout (milliseconds) of the url connection for external resources. This is
	 * used to ensure that locator doesn't spend too much time on slow end-point.
	 *
	 * @param connectionTimeout the connectionTimeout to set
	 */
	@Override
	public void setConnectionTimeout(final Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * @return the parallelPreprocessing
	 */
	public boolean isParallelPreprocessing() {

		if (parallelPreprocessing == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.parallelPreprocessing.getDefaultPropertyValue());
		} else {
			return parallelPreprocessing;
		}
	}

	/**
	 * @param parallelPreprocessing the parallelPreprocessing to set
	 */
	public void setParallelPreprocessing(final Boolean parallelPreprocessing) {
		this.parallelPreprocessing = parallelPreprocessing;
	}

	/**
	 * @return value of the flag responsible for handling empty group behavior.
	 */
	@Override
	public boolean isIgnoreEmptyGroup() {

		if (ignoreEmptyGroup == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.ignoreEmptyGroup.getDefaultPropertyValue());
		} else {
			return ignoreEmptyGroup;
		}
	}

	/**
	 * @param ignoreEmptyGroup flag for turning on/off failure when there is an
	 *                         empty group (nothing to process). This value is true
	 *                         by default, meaning that empty group will produce
	 *                         empty result (no exception).
	 */
	@Override
	public void setIgnoreEmptyGroup(final Boolean ignoreEmptyGroup) {
		this.ignoreEmptyGroup = ignoreEmptyGroup;
	}

	/**
	 * @return true if the processing failure should be ignored.
	 */
	public boolean isIgnoreFailingProcessor() {

		if (ignoreFailingProcessor == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.ignoreFailingProcessor.getDefaultPropertyValue());
		} else {
			return ignoreFailingProcessor;
		}
	}

	public void setIgnoreFailingProcessor(final Boolean ignoreFailingProcessor) {
		this.ignoreFailingProcessor = ignoreFailingProcessor;
	}

	@Override
	public final long getResourceWatcherUpdatePeriod() {

		if (resourceWatcherUpdatePeriod == null) {
			return (Long) ConfigConstants.resourceWatcherUpdatePeriod.getDefaultPropertyValue();
		} else {
			return resourceWatcherUpdatePeriod;
		}
	}

	@Override
	public final void setResourceWatcherUpdatePeriod(final Long resourceWatcherUpdatePeriod) {
		this.resourceWatcherUpdatePeriod = resourceWatcherUpdatePeriod;
	}

	/**
	 * @return flag indicating if the minimization is enabled. When this flag is
	 *         false, the minimization will be suppressed for all resources.
	 */
	@Override
	public boolean isMinimizeEnabled() {

		if (minimizeEnabled == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.minimizeEnabled.getDefaultPropertyValue());
		} else {
			return minimizeEnabled;
		}
	}

	@Override
	public void setMinimizeEnabled(final Boolean minimizeEnabled) {
		this.minimizeEnabled = minimizeEnabled;
	}

	/**
	 * @return true if the asynchronous resourceWatcher experimental feature is
	 *         enabled.
	 */
	public boolean isResourceWatcherAsync() {

		if (resourceWatcherAsync == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.resourceWatcherAsync.getDefaultPropertyValue());
		} else {
			return resourceWatcherAsync;
		}
	}

	public void setResourceWatcherAsync(final Boolean resourceWatcherAsync) {
		this.resourceWatcherAsync = resourceWatcherAsync;
	}

	public boolean isCacheHttpEnabled() {

		if (cacheHttpEnabled == null) {
			return BooleanUtils.isTrue((Boolean) ConfigConstants.cacheHttpEnabled.getDefaultPropertyValue());
		} else {
			return cacheHttpEnabled;
		}
	}

	public void setCacheHttpEnabled(final Boolean cacheHttpEnabled) {
		this.cacheHttpEnabled = cacheHttpEnabled;
	}

	public String getCacheHttpValue() {

		if (cacheHttpValue == null) {
			return (String) ConfigConstants.cacheHttpValue.getDefaultPropertyValue();
		} else {
			return cacheHttpValue;
		}
	}

	public void setCacheHttpValue(final String cacheHttpValue) {
		this.cacheHttpValue = cacheHttpValue;
	}

	public DeploymentMode getDeploymentMode() {

		if (deploymentMode == null) {
			return (DeploymentMode) ConfigConstants.deploymentMode.getDefaultPropertyValue();
		} else {
			return deploymentMode;
		}
	}

	public void setDeploymentMode(DeploymentMode deploymentMode) {
		this.deploymentMode = deploymentMode;
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, true);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE).toString();
	}

}
