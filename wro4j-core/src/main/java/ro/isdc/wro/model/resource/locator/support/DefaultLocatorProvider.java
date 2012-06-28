package ro.isdc.wro.model.resource.locator.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator.LocatorStrategy;


/**
 * Default implementation of {@link LocatorProvider} providing all {@link UriLocator} implementations from core module.
 * 
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class DefaultLocatorProvider
    implements LocatorProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceLocatorFactory> provideLocators() {
    final Map<String, ResourceLocatorFactory> map = new HashMap<String, ResourceLocatorFactory>();
    map.put(ClasspathUriLocator.ALIAS, new ClasspathResourceLocator());
    map.put(ServletContextUriLocator.ALIAS, new ServletContextUriLocator());
    map.put(ServletContextUriLocator.ALIAS_DISPATCHER_FIRST,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.DISPATCHER_FIRST));
    map.put(ServletContextUriLocator.ALIAS_SERVLET_CONTEXT_FIRST,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_FIRST));
    map.put(UrlUriLocator.ALIAS, new UrlUriLocator());
    return map;
  }
}
