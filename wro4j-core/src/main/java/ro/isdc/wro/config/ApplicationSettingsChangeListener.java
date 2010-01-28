/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.config;

/**
 * Listener which is notified about the change of some properties of {@link ApplicationSettings} object.
 *
 * @author Alex Objelean
 */
public interface ApplicationSettingsChangeListener {
	/**
	 * Invoked when the cachePeriod property is changed.
	 */
	void onCachePeriodChanged();
	/**
	 * Invoked when the modelPeriod property is changed.
	 */
	void onModelPeriodChanged();
}
