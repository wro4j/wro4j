/*
 * Copyright (C) 2011.
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Decorator for {@link WildcardStreamLocator}.
 *
 * @author Alex Objelean
 * @created 19 July 2011
 * @since 1.3.9
 */
public class WildcardStreamLocatorDecorator
    implements WildcardStreamLocator {
  private final WildcardStreamLocator decorated;

  public WildcardStreamLocatorDecorator(final WildcardStreamLocator decorated) {
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasWildcard(final String uri) {
    return decorated.hasWildcard(uri);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locateStream(final String uri, final File folder)
      throws IOException {
    return decorated.locateStream(uri, folder);
  }

  /**
   * {@inheritDoc}
   */
  public void handleFoundFiles(final Collection<File> files) {
    decorated.handleFoundFiles(files);
  }
}
