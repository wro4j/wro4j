/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Holds a list of uri locators. The uriLocator will be created based on the first
 * uriLocator from the supplied list which will accept the url.
 *
 * @author Alex Objelean
 * @created Created on Nov 4, 2008
 */
public final class UriLocatorFactory {
  private final List<UriLocator> uriLocators = new ArrayList<UriLocator>();
  private Injector injector;


  /**
   * @param injector {@link Injector} used to inject the fields in each UriLocator.
   */
  public UriLocatorFactory(final Injector injector) {
    this.injector = injector;
    injector.inject(this);
  }


  /**
   * Locates an InputStream for the given uri.
   *
   * @param uri to locate.
   * @return {@link InputStream} of the resource.
   * @throws IOException if uri is invalid or resource couldn't be located.
   */
  public InputStream locate(final String uri)
    throws IOException {
    final UriLocator uriLocator = getInstance(uri);
    if (uriLocator == null) {
      throw new IOException("No locator is capable of handling uri: " + uri);
    }
    return uriLocator.locate(uri);
  }


  /**
   * @param uri to handle by the locator.
   * @return an instance of {@link UriLocator} which is capable of handling provided uri. Returns null if no locator
   *         found.
   */
  private UriLocator getInstance(final String uri) {
    for (final UriLocator uriLocator : uriLocators) {
      if (uriLocator.accept(uri)) {
        return uriLocator;
      }
    }
    return null;
  }


  /**
   * Add a single resource to the list of supported resource locators.
   *
   * @param uriLocator {@link UriLocator} object to add.
   */
  private final void addUriLocator(final UriLocator uriLocator) {
    if (uriLocator == null) {
      throw new IllegalArgumentException("ResourceLocator cannot be null!");
    }
    injector.inject(uriLocator);
    // inject duplicateResourceDetector
    uriLocators.add(uriLocator);
  }


  /**
   * Allow adding more than one uriLocators.
   *
   * @param locators list of {@link UriLocator} arguments.
   */
  public final void addUriLocator(final UriLocator... locators) {
    for (final UriLocator locator : locators) {
      addUriLocator(locator);
    }
  }
}
