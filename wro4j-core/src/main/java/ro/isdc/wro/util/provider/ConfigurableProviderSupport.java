package ro.isdc.wro.util.provider;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;


/**
 * Default implementation of {@link ConfigurableProvider} which provides nothing by default.
 *
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class ConfigurableProviderSupport
    implements ConfigurableProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    return new HashMap<String, ResourcePreProcessor>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return new HashMap<String, ResourcePostProcessor>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, NamingStrategy> provideNamingStrategies() {
    return new HashMap<String, NamingStrategy>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, HashStrategy> provideHashStrategies() {
    return new HashMap<String, HashStrategy>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, UriLocator> provideLocators() {
    return new HashMap<String, UriLocator>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, CacheStrategy<CacheKey, CacheValue>> provideCacheStrategies() {
    return new HashMap<String, CacheStrategy<CacheKey, CacheValue>>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, RequestHandler> provideRequestHandlers() {
    return new HashMap<String, RequestHandler>();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, WroModelFactory> provideModelFactories() {
    return new HashMap<String, WroModelFactory>();
  }
}
