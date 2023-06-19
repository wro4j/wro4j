package ro.isdc.wro.extensions.locator.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;
import ro.isdc.wro.extensions.locator.WebjarsUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.Ordered;

/**
 * Default implementation of {@link LocatorProvider} providing all
 * {@link UriLocator} implementations from core module.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class DefaultLocatorProvider
    implements LocatorProvider, Ordered {

  @Override
  public Map<String, UriLocator> provideLocators() {
    final Map<String, UriLocator> map = new HashMap<String, UriLocator>();
    map.put(WebjarUriLocator.ALIAS, new WebjarUriLocator());
    map.put(WebjarsUriLocator.ALIAS, new WebjarsUriLocator());
    return map;
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST;
  }
}
