/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import javax.inject.Inject;

import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * An {@link UriLocator} which knows how to handle wildcards and provides {@link WildcardStreamLocator} implementation.
 *
 * @author Alex Objelean
 * @created Created on May 09, 2010
 */
public abstract class WildcardUriLocatorSupport implements UriLocator {
  @Inject
  private DuplicateResourceDetector duplicateResourceDetector;
  /**
   * Wildcard stream locator implementation.
   */
  private WildcardStreamLocator wildcardStreamLocator;

  /**
   * @return default implementation of {@link WildcardStreamLocator}.
   */
  protected WildcardStreamLocator newWildcardStreamLocator() {
    return new DefaultWildcardStreamLocator(duplicateResourceDetector);
  }

  /**
   * @return the wildcardStreamLocator
   */
  protected final WildcardStreamLocator getWildcardStreamLocator() {
    if (wildcardStreamLocator == null) {
      wildcardStreamLocator = newWildcardStreamLocator();
    }
    return this.wildcardStreamLocator;
  }
}
