/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;

/**
 * WROManagerFactory.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
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
