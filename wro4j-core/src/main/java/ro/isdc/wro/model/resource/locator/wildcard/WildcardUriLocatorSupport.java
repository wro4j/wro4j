/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * An {@link UriLocator} which knows how to handle wildcards and provides {@link WildcardStreamLocator} implementation.
 *
 * @author Alex Objelean
 * @created Created on May 09, 2010
 */
public abstract class WildcardUriLocatorSupport implements UriLocator {
  /**
   * Wildcard stream locator implementation.
   */
  protected WildcardStreamLocator wildcardStreamLocator;

  /**
   * Default constructor.
   */
  public WildcardUriLocatorSupport() {
    wildcardStreamLocator = newWildcardStreamLocator();
  }

  /**
   * @return default implementation of {@link WildcardStreamLocator}.
   */
  protected WildcardStreamLocator newWildcardStreamLocator() {
    return new DefaultWildcardStreamLocator();
  }

  /**
   * @return the wildcardStreamLocator
   */
  protected final WildcardStreamLocator getWildcardStreamLocator() {
    return this.wildcardStreamLocator;
  }
}
