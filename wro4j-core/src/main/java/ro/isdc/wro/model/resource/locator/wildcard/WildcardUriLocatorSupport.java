/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * An {@link UriLocator} which knows how to handle wildcards and provides {@link WildcardStreamLocator} implementation.
 *
 * @author Alex Objelean
 */
public abstract class WildcardUriLocatorSupport
  implements UriLocator {
  /**
   * Wildcard stream locator implementation.
   */
  private WildcardStreamLocator wildcardStreamLocator;
  /**
   * Flag used to enable/disable wildcards.
   */
  private boolean enableWildcards = true;
  /**
   * @return default implementation of {@link WildcardStreamLocator}.
   */
  public WildcardStreamLocator newWildcardStreamLocator() {
    return new DefaultWildcardStreamLocator() {
      @Override
      public boolean hasWildcard(final String uri) {
        return enableWildcards && super.hasWildcard(uri);
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
  public WildcardUriLocatorSupport setEnableWildcards(boolean enableWildcards) {
    this.enableWildcards = enableWildcards;
    return this;
  }

  /**
   * @return when false, the locator will ignore wildcard resources.
   */
  public boolean isEnableWildcards() {
    return enableWildcards;
  }
}
