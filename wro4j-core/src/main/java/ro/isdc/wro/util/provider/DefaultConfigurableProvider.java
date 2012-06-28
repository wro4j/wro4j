package ro.isdc.wro.util.provider;

import java.util.Map;

import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.DefaultLocatorProvider;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.support.DefaultProcessorProvider;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.DefaultHashStrategyProvider;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.DefaultNamingStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;

/**
 * Default implementation of {@link ConfigurableProviderSupport} which contributes with components from core module.
 * 
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class DefaultConfigurableProvider
    extends ConfigurableProviderSupport {
  private final ProcessorProvider processorProvider = new DefaultProcessorProvider();
  private final NamingStrategyProvider namingStrategyProvider = new DefaultNamingStrategyProvider();
  private final HashStrategyProvider hashBuilderProvider = new DefaultHashStrategyProvider();
  private final LocatorProvider locatorProvider = new DefaultLocatorProvider();
  
  /**
   * {@inheritDoc}
   */
  @Override
  public java.util.Map<String,ResourceProcessor> providePreProcessors() {
    return processorProvider.providePreProcessors();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ResourceProcessor> providePostProcessors() {
    return processorProvider.providePostProcessors();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, HashStrategy> provideHashStrategies() {
    return hashBuilderProvider.provideHashStrategies();
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
  public Map<String, ResourceLocatorFactory> provideLocators() {
    return locatorProvider.provideLocators();
  }
}
