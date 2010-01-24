/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

/**
 * Factory used to create {@link WroManager} objects.
 *
 * @author Alex Objelean
 * @created Created on Oct 31, 2008
 */
public interface WroManagerFactory {
  /**
   * Creates a instance of {@link WroManager} object.
   *
   * @return Configured {@link WroManager}.
   */
  public WroManager getInstance();

  /**
   * Called by filter indicating that it is being taken out of service.
   */
  public void destroy();
}
