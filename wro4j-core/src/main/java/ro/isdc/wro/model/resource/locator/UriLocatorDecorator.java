/*
 * Copyright (C) 2011 wro4j. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;


/**
 * A simple decorator for {@link UriLocator}
 *
 * @author Alex Objelean
 * @created 19 Jul 2011
 * @since 1.3.9
 */
public class UriLocatorDecorator
    extends WildcardUriLocatorSupport {

  private final UriLocator decorated;

  public UriLocatorDecorator(final UriLocator decorated) {
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    return decorated.locate(uri);
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return decorated.accept(uri);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WildcardStreamLocator newWildcardStreamLocator() {
    if (decorated instanceof WildcardUriLocatorSupport) {
      return ((WildcardUriLocatorSupport)decorated).newWildcardStreamLocator();
    }
    throw new WroRuntimeException("Decorated UriLocator doesn't support wildcards!");
  }
}
