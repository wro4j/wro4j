/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Decorates a {@link ResourceLocator}.
 * 
 * @author Alex Objelean
 * @created 1 apr 2011
 * @since 1.4.0
 */
public class ResourceLocatorDecorator
    extends AbstractDecorator<ResourceLocator>
    implements ResourceLocator {
  public ResourceLocatorDecorator(final ResourceLocator decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getInputStream()
    throws IOException {
    return getDecoratedObject().getInputStream();
  }


  /**
   * {@inheritDoc}
   */
  public long lastModified() {
    return getDecoratedObject().lastModified();
  }


  /**
   * {@inheritDoc}
   */
  public ResourceLocator createRelative(final String relativePath)
    throws IOException {
    return getDecoratedObject().createRelative(relativePath);
  }

}
