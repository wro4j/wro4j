/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

/**
 * WROManagerFactory.java.
 *
 * @author Alex Objelean
 * @created Created on Oct 31, 2008
 */
public interface WroManagerFactory {
  /**
   * Creates a instance of {@link WroManager} object.
   *
   * @return Configured WROManager.
   */
  public WroManager getInstance();
}
