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
	 * @param period number of seconds. Zero or less, means never.
	 */
	void setModelUpdatePeriod(final long period);

	/**
	 * @return period in seconds when the cache containing merged resources by group should be updated.
	 */
	long getCacheUpdatePeriod();

	/**
	 * Set after how many seconds the cache containing merged resources by group name should be updated.
	 *
	 * @param period number of seconds. Zero or less, means never.
	 */
	void setCacheUpdatePeriod(final long period);

	/**
	 * @return true if Gzip is Enabled.
	 */
	boolean isGzipEnabled();

	/**
	 * @param enable if true, gzip will be enabled, otherwise will be disabled.
	 */
	void setGzipEnabled(final boolean enable);

	/**
	 * @return the state of debug flag.
	 */
	boolean isDebug();

  /**
   * Set the debug mode.
   *
   * @param debug if true, the wro4j will run in DEVELOPMENT MODE.
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
}
