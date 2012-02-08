/*
 * Copyright (c) 2008. All rights reserved.
/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardExpanderHandlerAware;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.util.Function;


/**
 * Implements most of the {@link ResourceLocator} methods and provides basic wildcard support.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public abstract class AbstractResourceLocator
  implements ResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractResourceLocator.class);
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
  
  /**
   * Sets the Exapnder handler on this locator if supported.
   */
  public final void setWildcardExpanderHandler(final Function<Collection<File>, Void> handler) {
    //use getter to ensure we are using a not null instance
    if (getWildcardStreamLocator() instanceof WildcardExpanderHandlerAware) {
      ((WildcardExpanderHandlerAware) wildcardStreamLocator).setWildcardExpanderHandler(handler);
    } else {
      LOG.debug("[WARNING] The WildcardExpanderHandler won't be used because wildcardStreamLocator is not aware of ExpanderHandler!");
    }
  }
}
