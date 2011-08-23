/*
 * Copyright (c) 2008. All rights reserved.
/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;


/**
 * Implements most of the {@link ResourceLocator} methods and provides basic wildcard support.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public abstract class AbstractResourceLocator
  implements ResourceLocator {
  /**
   * Wildcard stream locator implementation.
   */
  private WildcardStreamLocator wildcardStreamLocator;

  /**
   * @return default implementation of {@link WildcardStreamLocator}.
   */
  public WildcardStreamLocator newWildcardStreamLocator() {
    return new DefaultWildcardStreamLocator() {
      @Override
      public boolean hasWildcard(final String uri) {
        return !disableWildcards() && super.hasWildcard(uri);
      }
    };
  }

  /**
   * @return the wildcardStreamLocator
   */
  public final WildcardStreamLocator getWildcardStreamLocator() {
    if (wildcardStreamLocator == null) {
      wildcardStreamLocator = newWildcardStreamLocator();
    }
    return this.wildcardStreamLocator;
  }

  /**
   * Allows disabling wildcard support. By default wildcard support is enabled.
   */
  protected boolean disableWildcards() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public long lastModified() {
    return 0;
  }


  /**
   * {@inheritDoc}
   */
  public ResourceLocator createRelative(final String relativePath)
    throws IOException {
    throw new IOException("Cannot find relative resource for: " + relativePath);
  }
}
