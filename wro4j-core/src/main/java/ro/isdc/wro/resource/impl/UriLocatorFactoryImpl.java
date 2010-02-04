/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.resource.impl;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * Default implementation of UriLocator. Holds a list of uri locators. The
 * uriLocator will be created based on the first uriLocator from the supplied
 * list which will accept the url.
 *
 * @author Alex Objelean
 * @created Created on Nov 4, 2008
 */
public final class UriLocatorFactoryImpl implements UriLocatorFactory {
  /**
   * List of resource readers.
   */
  private List<UriLocator> uriLocators = new ArrayList<UriLocator>();

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    for (final UriLocator uriLocator : uriLocators) {
      if (uriLocator.accept(uri)) {
        return uriLocator;
      }
    }
    throw new WroRuntimeException(
        "Cannot find a uriLocator for the following uri: " + uri);
  }

  /**
   * Add a single resource to the list of supported resource locators.
   *
   * @param uriLocator
   *          {@link UriLocator} object to add.
   */
  public final void addUriLocator(final UriLocator uriLocator) {
    if (uriLocator == null) {
      throw new IllegalArgumentException("ResourceLocator cannot be null!");
    }
    uriLocators.add(uriLocator);
  }

  /**
   * @param uriLocators
   *          the resourceLocators to set
   */
  public final void setUriLocators(final List<UriLocator> uriLocators) {
    if (uriLocators == null) {
      throw new IllegalArgumentException("uriLocators list cannot be null!");
    }
    this.uriLocators = uriLocators;
  }
}
