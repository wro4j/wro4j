package ro.isdc.wro.maven.plugin.support.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.locator.StandaloneServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.Ordered;


/**
 * Responsible for custom providing custom locators from maven plugin module.
 *
 * @author Alex Objelean
 * @since 1.7.10
 */
public class DefaultLocatorProvider
    implements LocatorProvider, Ordered {

  public Map<String, UriLocator> provideLocators() {
    final Map<String, UriLocator> map = new HashMap<String, UriLocator>();
    map.put(StandaloneServletContextUriLocator.ALIAS, new StandaloneServletContextUriLocator());
    return map;
  }

  /**
   * The order is slightly higher than the one provided by default by core module. This is important in order to
   * override custom configurations.
   */
  public int getOrder() {
    return Ordered.LOWEST;
  }
}
