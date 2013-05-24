package ro.isdc.wro.extensions.locator.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.extensions.locator.WebjarResourceLocatorFactory;
import ro.isdc.wro.extensions.locator.WebjarResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.Ordered;


/**
 * Default implementation of {@link LocatorProvider} providing all {@link UriLocator} implementations from core module.
 *
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class DefaultLocatorProvider
    implements LocatorProvider, Ordered {
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ResourceLocatorFactory> provideLocators() {
    final Map<String, ResourceLocatorFactory> map = new HashMap<String, ResourceLocatorFactory>();
    map.put(WebjarResourceLocator.ALIAS, new WebjarResourceLocatorFactory());
    return map;
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST;
  }
}
