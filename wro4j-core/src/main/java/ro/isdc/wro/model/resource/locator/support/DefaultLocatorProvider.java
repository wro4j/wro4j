package ro.isdc.wro.model.resource.locator.support;

import java.util.LinkedHashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.SaasUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator.LocatorStrategy;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
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

  public Map<String, UriLocator> provideLocators() {
    // use linkedHashMap to preserve the order of providers.
    final Map<String, UriLocator> map = new LinkedHashMap<String, UriLocator>();
    map.put(ClasspathUriLocator.ALIAS, new ClasspathUriLocator());
    map.put(ServletContextUriLocator.ALIAS, new ServletContextUriLocator());
    map.put(ServletContextUriLocator.ALIAS_DISPATCHER_FIRST,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.DISPATCHER_FIRST));
    map.put(ServletContextUriLocator.ALIAS_SERVLET_CONTEXT_FIRST,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_FIRST));
    map.put(ServletContextUriLocator.ALIAS_SERVLET_CONTEXT_ONLY,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_ONLY));
    map.put(UrlUriLocator.ALIAS, new UrlUriLocator());
    map.put(SaasUriLocator.ALIAS, new SaasUriLocator());
    return map;
  }

  public int getOrder() {
    return Ordered.LOWEST;
  }
}
