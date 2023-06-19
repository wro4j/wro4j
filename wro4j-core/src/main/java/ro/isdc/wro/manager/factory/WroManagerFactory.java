/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.config.support.WroConfigurationChangeListener;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.util.ObjectFactory;

/**
 * Factory used to create {@link WroManager} objects.
 *
 * @author Alex Objelean
 */
public interface WroManagerFactory extends ObjectFactory<WroManager>, WroConfigurationChangeListener {
  /**
   * Called by filter indicating that it is being taken out of service.
   */
  public void destroy();
}
