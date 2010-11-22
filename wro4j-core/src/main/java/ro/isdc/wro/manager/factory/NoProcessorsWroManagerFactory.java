/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.resource.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;

/**
 * An implementation with no processors set.
 *
 * @author Alex Objelean
 * @created Created on May 4, 2010
 */
public final class NoProcessorsWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected WroManager newWroManager() {
    return new WroManager() {
      @Override
      protected UriLocatorFactory newUriLocatorFactory() {
        final UriLocatorFactory factory = new SimpleUriLocatorFactory().addUriLocator(new ServletContextUriLocator()).addUriLocator(
            new ClasspathUriLocator()).addUriLocator(new UrlUriLocator());
        return factory;
      }
    };
  }
}
