/*
 * Copyright (C) 2011 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.locator.support.DynamicServletContextResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;


/**
 * Default implementation of {@link ResourceLocatorFactory}. This implementation relies on wro4j {@link Context} object
 * and cannot be used without it. The algorithm of returning best suited {@link ResourceLocator} is based on uri
 * analysis. If the uri starts with a prefix some {@link ResourceLocator} can handle, it will be used. Eventually, the
 * {@link UrlResourceLocator} is used if no other best suited locator is found.
 *
 * @author Alex Objelean
 * @created 31 Mar 2011
 * @since 1.4.0
 */
public class DefaultResourceLocatorFactory
    implements ResourceLocatorFactory {
  /**
   * {@inheritDoc}
   */
  public ResourceLocator locate(final String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("uri cannot be null!");
    }
    if (uri.startsWith(ClasspathResourceLocator.PREFIX)) {
      return new ClasspathResourceLocator(uri);
    }
    if (uri.startsWith(ServletContextResourceLocator.PREFIX)) {
      final Context ctx = Context.get();
      return new DynamicServletContextResourceLocator(ctx.getRequest(), ctx.getResponse(), ctx.getServletContext(), uri);
    }
    return new UrlResourceLocator(uri);
  }

}
