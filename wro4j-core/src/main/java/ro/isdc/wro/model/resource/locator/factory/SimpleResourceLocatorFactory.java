package ro.isdc.wro.model.resource.locator.factory;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.model.resource.locator.ResourceLocator;


/**
 * Holds a list of {@link ResourceLocatorFactory} and uses the first one which returns a not null
 * {@link ResourceLocator}.
 * 
 * @author Alex Objelean
 * @created 9 Jul 2012
 * @since 2.0
 */
public class SimpleResourceLocatorFactory
    extends AbstractResourceLocatorFactory {
  private final List<ResourceLocatorFactory> locatorFactories = new ArrayList<ResourceLocatorFactory>();

  /**
   * {@inheritDoc}
   */
  public final ResourceLocator getLocator(final String uri) {
    for (ResourceLocatorFactory locatorFactory : locatorFactories) {
      final ResourceLocator locator = locatorFactory.getLocator(uri);
      if (locator != null) {
        return locator;
      }
    }
    return null;
  }
  
  /**
   * Add a single factory to the list of available factories.
   */
  public final SimpleResourceLocatorFactory addFactory(final ResourceLocatorFactory locatorFactory) {
    locatorFactories.add(locatorFactory);
    return this;
  }
}
