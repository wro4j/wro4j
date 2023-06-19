package ro.isdc.wro.util.provider;

import java.util.Map;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.spi.CacheStrategyProvider;
import ro.isdc.wro.cache.spi.DefaultCacheStrategyProvider;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.DefaultRequestHandlerProvider;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.model.factory.WroModelFactory;
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
import ro.isdc.wro.model.spi.DefaultModelFactoryProvider;
import ro.isdc.wro.model.spi.ModelFactoryProvider;
import ro.isdc.wro.util.Ordered;

/**
 * Default implementation of {@link ConfigurableProviderSupport} which contributes with components from core module.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class DefaultConfigurableProvider
    extends ConfigurableProviderSupport implements Ordered {
  private final ProcessorProvider processorProvider = new DefaultProcessorProvider();
  private final NamingStrategyProvider namingStrategyProvider = new DefaultNamingStrategyProvider();
  private final HashStrategyProvider hashStrategyProvider = new DefaultHashStrategyProvider();
  private final LocatorProvider locatorProvider = new DefaultLocatorProvider();
  private final CacheStrategyProvider cacheStrategyProvider = new DefaultCacheStrategyProvider();
  private final RequestHandlerProvider requestHandlerProvider = new DefaultRequestHandlerProvider();
  private final ModelFactoryProvider modelFactoryProvider = new DefaultModelFactoryProvider();
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String,ResourcePreProcessor> providePreProcessors() {
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
  public Map<String, CacheStrategy<CacheKey, CacheValue>> provideCacheStrategies() {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, WroModelFactory> provideModelFactories() {
    return modelFactoryProvider.provideModelFactories();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getOrder() {
    //The lowest order is used to allow custom provider to override providers with the same name.
    return Ordered.LOWEST;
  }
}
