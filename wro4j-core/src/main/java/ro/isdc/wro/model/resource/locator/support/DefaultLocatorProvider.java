package ro.isdc.wro.model.resource.locator.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.ClasspathResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ServletContextResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UrlResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator.LocatorStrategy;
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
  public Map<String, ResourceLocatorFactory> provideLocators() {
    final Map<String, ResourceLocatorFactory> map = new HashMap<String, ResourceLocatorFactory>();
    map.put(ClasspathResourceLocator.ALIAS, new ClasspathResourceLocatorFactory());
    map.put(ServletContextResourceLocator.ALIAS, new ServletContextResourceLocatorFactory());
    map.put(ServletContextResourceLocator.ALIAS_DISPATCHER_FIRST, new ServletContextResourceLocatorFactory() {
      @Override
      protected ResourceLocator newLocator(final String uri) {
        return new ServletContextResourceLocator(Context.get().getServletContext(), uri).setLocatorStrategy(LocatorStrategy.DISPATCHER_FIRST);
      }
    });
    map.put(ServletContextResourceLocator.ALIAS_SERVLET_CONTEXT_FIRST, new ServletContextResourceLocatorFactory() {
      @Override
      protected ResourceLocator newLocator(final String uri) {
        return new ServletContextResourceLocator(Context.get().getServletContext(), uri).setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_FIRST);
      }
    });
    map.put(UrlResourceLocator.ALIAS, new UrlResourceLocatorFactory());
    return map;
  }

  public int getOrder() {
    return Ordered.LOWEST;
  }
}
