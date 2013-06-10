/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.util.List;

import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Default implementation of {@link UriLocatorFactory}. Holds most used locators.
 *
 * @author Alex Objelean
 * @created 15 May 2011
 * @since 1.3.7
 */
public final class DefaultUriLocatorFactory extends SimpleUriLocatorFactory {
  public DefaultUriLocatorFactory() {
    final List<LocatorProvider> providers = ProviderFinder.of(LocatorProvider.class).find();
    for (final LocatorProvider provider : providers) {
      addLocators(provider.provideLocators().values());
    }
  }
}
