package ro.isdc.wro.model.resource.locator.support;

import java.util.Map;
import java.util.TreeMap;

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
    final Map<String, ResourceLocatorFactory> map = new TreeMap<String, ResourceLocatorFactory>();
    map.put(ClasspathResourceLocator.ALIAS, new ClasspathResourceLocatorFactory());
    map.put(ServletContextResourceLocator.ALIAS, new ServletContextResourceLocatorFactory());
    map.put(ServletContextResourceLocator.ALIAS_DISPATCHER_FIRST, locatorWithStrategy(LocatorStrategy.DISPATCHER_FIRST));
    map.put(ServletContextResourceLocator.ALIAS_SERVLET_CONTEXT_FIRST, locatorWithStrategy(LocatorStrategy.SERVLET_CONTEXT_FIRST));
    map.put(ServletContextResourceLocator.ALIAS_SERVLET_CONTEXT_ONLY, locatorWithStrategy(LocatorStrategy.SERVLET_CONTEXT_ONLY));
    map.put(UrlResourceLocator.ALIAS, new UrlResourceLocatorFactory());
    return map;
  }

  private ServletContextResourceLocatorFactory locatorWithStrategy(final LocatorStrategy strategy) {
    return new ServletContextResourceLocatorFactory() {
      @Override
      protected ResourceLocator newLocator(final String uri) {
        return new ServletContextResourceLocator(Context.get().getServletContext(), uri).setLocatorStrategy(strategy);
      }
    };
  }

  public int getOrder() {
    return Ordered.LOWEST;
  }
}
