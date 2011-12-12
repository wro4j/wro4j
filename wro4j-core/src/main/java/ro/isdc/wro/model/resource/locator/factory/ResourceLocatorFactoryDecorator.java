/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.locator.ResourceLocator;

/**
 * Decorator for {@link ResourceLocatorFactory}.
 *
 * @author Alex Objelean
 * @created 31 Mar 2011
 * @since 1.4.0
 */
public class ResourceLocatorFactoryDecorator
  implements ResourceLocatorFactory {
  private final ResourceLocatorFactory decorated;

  public ResourceLocatorFactoryDecorator(final ResourceLocatorFactory decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public ResourceLocator locate(final String uri) {
    return decorated.locate(uri);
  }
}
