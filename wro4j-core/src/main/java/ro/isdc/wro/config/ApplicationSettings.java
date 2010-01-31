/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.management.NotificationBroadcasterSupport;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Defines MBean which manage configuration.
 * @author Alex Objelean
 */
public final class ApplicationSettings extends NotificationBroadcasterSupport
  implements ApplicationSettingsMBean {
	/**
	 * By default period values are 0, that means that the associated scheduler never runs.
	 */
  private long cacheUpdatePeriod = 90;
  private long modelUpdatePeriod;
  /**
   * Gzip enable flag.
   */
  private boolean gzipEnabled = true;
  /**
   * Listeners for the change of cache & model period properties.
   */
  private final List<PropertyChangeListener> cacheUpdatePeriodListeners = new ArrayList<PropertyChangeListener>();
  private final List<PropertyChangeListener> modelUpdatePeriodListeners = new ArrayList<PropertyChangeListener>();

  /**
   * @return the name of the object used to register the MBean.
   */
  public static String getObjectName() {
    return ApplicationSettings.class.getPackage().getName() + ".jmx:type=" + ApplicationSettings.class.getSimpleName();
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
	 * @param newValue value to set.
	 */
	private void reloadCacheWithNewValue(final Long newValue) {
		final long newValueAsPrimitive = newValue == null ? getModelUpdatePeriod() : newValue;
		for (final PropertyChangeListener listener : cacheUpdatePeriodListeners) {
  		final PropertyChangeEvent event = new PropertyChangeEvent(this, "cache", getCacheUpdatePeriod(), newValueAsPrimitive);
			listener.propertyChange(event);
		}
	}

  /**
   * {@inheritDoc}
   */
  public void reloadModel() {
  	reloadModelWithNewValue(null);
  }


	/**
	 * Notify all listeners about cachePeriod property changed. If passed newValue is null, the oldValue is taken as new
	 * value. This is the case when the reloadModel is invoked.
	 *
	 * @param newValue value to set.
	 */
	private void reloadModelWithNewValue(final Long newValue) {
		final long newValueAsPrimitive = newValue == null ? getModelUpdatePeriod() : newValue;
  	for (final PropertyChangeListener listener : modelUpdatePeriodListeners) {
  		final PropertyChangeEvent event = new PropertyChangeEvent(this, "model", getModelUpdatePeriod(), newValueAsPrimitive);
			listener.propertyChange(event);
		}
	}

	/**
	 * Register a listener which is notified when the modelUpdate period value is changed.
	 *
	 * @param listener to add.
	 */
	public void registerModelUpdatePeriodChangeListener(final PropertyChangeListener listener) {
		modelUpdatePeriodListeners.add(listener);
	}

	/**
	 * Register a listener which is notified when the modelUpdate period value is changed.
	 *
	 * @param listener to add.
	 */
	public void registerCacheUpdatePeriodChangeListener(final PropertyChangeListener listener) {
		cacheUpdatePeriodListeners.add(listener);
	}

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
  	return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
