package ro.isdc.wro.util.provider;

import java.util.Map;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.spi.CacheStrategyProvider;
import ro.isdc.wro.cache.spi.DefaultCacheStrategyProvider;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.DefaultRequestHandlerProvider;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.DefaultLocatorProvider;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.DefaultProcessorProvider;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.DefaultHashStrategyProvider;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.DefaultNamingStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;
import ro.isdc.wro.util.Ordered;

/**
 * Default implementation of {@link ConfigurableProviderSupport} which contributes with components from core module.
 * 
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class DefaultConfigurableProvider
    extends ConfigurableProviderSupport implements Ordered {
  private ProcessorProvider processorProvider = new DefaultProcessorProvider();
  private NamingStrategyProvider namingStrategyProvider = new DefaultNamingStrategyProvider();
  private HashStrategyProvider hashStrategyProvider = new DefaultHashStrategyProvider();
  private LocatorProvider locatorProvider = new DefaultLocatorProvider();
  private CacheStrategyProvider cacheStrategyProvider = new DefaultCacheStrategyProvider();
  private RequestHandlerProvider requestHandlerProvider = new DefaultRequestHandlerProvider();
  
  /**
   * {@inheritDoc}
   */
  public java.util.Map<String,ResourcePreProcessor> providePreProcessors() {
    return processorProvider.providePreProcessors();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return processorProvider.providePostProcessors();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, HashStrategy> provideHashStrategies() {
    return hashStrategyProvider.provideHashStrategies();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, NamingStrategy> provideNamingStrategies() {
    return namingStrategyProvider.provideNamingStrategies();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, CacheStrategy> provideCacheStrategies() {
    return cacheStrategyProvider.provideCacheStrategies();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, UriLocator> provideLocators() {
    return locatorProvider.provideLocators();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, RequestHandler> provideRequestHandlers() {
    return requestHandlerProvider.provideRequestHandlers();
  }

  public int getOrder() {
    return Ordered.LOWEST;
  }
}
