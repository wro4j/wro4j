/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.Ordered;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Default implementation of {@link UriLocatorFactory}. It loads all locators provided as SPI and sort them using
 * {@link Ordered#DESCENDING_COMPARATOR} (from highest to lowest priority). If there is more than one locator with same alias,
 * the resulted list won't remove duplicates.
 *
 * @author Alex Objelean
 * @since 1.3.7
 */
public final class DefaultUriLocatorFactory extends SimpleUriLocatorFactory {
  public DefaultUriLocatorFactory() {
    final List<LocatorProvider> providers = ProviderFinder.of(LocatorProvider.class).find();
    final List<UriLocator> locators = new ArrayList<UriLocator>();

    for (final LocatorProvider provider : providers) {
      locators.addAll(provider.provideLocators().values());
    }

    addLocators(locators);
  }
}
