/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.config.support;

import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * Listener which is notified about the change of some properties of {@link WroConfiguration} object.
 *
 * @author Alex Objelean
 */
public interface WroConfigurationChangeListener {
  /**
   * Invoked when the cachePeriod property is changed.
   *
   * @param value number of seconds used by scheduler to trigger cache change.
   */
	void onCachePeriodChanged(long value);
	/**
	 * Invoked when the modelPeriod property is changed.
	 *
	 * @param value number of seconds used by scheduler to trigger model change.
	 */
	void onModelPeriodChanged(long value);
}
