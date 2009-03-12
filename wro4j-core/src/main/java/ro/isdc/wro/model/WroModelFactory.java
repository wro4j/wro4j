/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model;

import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * ResourcesModelFactory.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public interface WroModelFactory {
  /**
   * Creates a {@link WroModel} object. The concrete implementation must
   * synchronize the instantiation of the model.
   * 
   * @param uriLocatorFactory
   *          responsible for creating UriLocators for each Resource.
   * @return an instance of {@link WroModel}.
   */
  WroModel getInstance(final UriLocatorFactory uriLocatorFactory);
}
