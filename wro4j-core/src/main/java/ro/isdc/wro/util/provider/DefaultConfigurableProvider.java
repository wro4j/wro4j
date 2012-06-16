package ro.isdc.wro.util.provider;

import java.util.Map;

import ro.isdc.wro.model.resource.processor.ProcessorProvider;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.DefaultProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.DefaultHashBuilderProvider;
import ro.isdc.wro.model.resource.support.hash.HashBuilder;
import ro.isdc.wro.model.resource.support.hash.HashBuilderProvider;
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
  private ProcessorProvider processorProvider = new DefaultProcessorProvider();
  private NamingStrategyProvider namingStrategyProvider = new DefaultNamingStrategyProvider();
  private HashBuilderProvider hashBuilderProvider = new DefaultHashBuilderProvider();
  
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
  public Map<String, HashBuilder> provideHashBuilders() {
    return hashBuilderProvider.provideHashBuilders();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, NamingStrategy> provideNamingStrategies() {
    return namingStrategyProvider.provideNamingStrategies();
  }
}
