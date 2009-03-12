/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;

/**
 * Default impelmentation of {@link WroManagerFactory} interface. This factory
 * returns a {@link WroManager} object injected with IoC.
 * 
 * @author Alexandru.Objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Dec 5, 2008
 */
public class WroManagerFactoryImpl implements WroManagerFactory {
  /**
   * {@link WroManager} object.
   */
  private WroManager wroManager;

  /**
   * {@inheritDoc}
   */
  public WroManager getInstance() {
    return wroManager;
  }

  /**
   * @return the wroManager
   */
  public final WroManager getWroManager() {
    return wroManager;
  }

  /**
   * @param wroManager
   *          the wroManager to set
   */
  public final void setWroManager(final WroManager wroManager) {
    this.wroManager = wroManager;
  }
}
