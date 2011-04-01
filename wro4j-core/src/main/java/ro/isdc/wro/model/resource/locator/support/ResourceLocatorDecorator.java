/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.ResourceLocator;


/**
 * Decorates a {@link ResourceLocator}.
 *
 * @author Alex Objelean
 * @created 1 apr 2011
 * @since 1.4.0
 */
public class ResourceLocatorDecorator
  implements ResourceLocator {
  /**
   * {@link ResourceLocator} to decorate.
   */
  private ResourceLocator decorated;

  public ResourceLocatorDecorator(final ResourceLocator decorated) {
    if (decorated == null) {
      throw new IllegalArgumentException("ResourceLocator cannot be null!");
    }
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getInputStream()
    throws IOException {
    return decorated.getInputStream();
  }


  /**
   * {@inheritDoc}
   */
  public long lastModified() {
    return decorated.lastModified();
  }


  /**
   * {@inheritDoc}
   */
  public ResourceLocator createRelative(final String relativePath)
    throws IOException {
    return decorated.createRelative(relativePath);
  }

}
