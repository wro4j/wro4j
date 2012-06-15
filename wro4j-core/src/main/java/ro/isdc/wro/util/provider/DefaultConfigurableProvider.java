package ro.isdc.wro.util.provider;

import java.util.Map;

import ro.isdc.wro.model.resource.processor.ProcessorsProvider;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.DefaultProcessorsProvider;
import ro.isdc.wro.model.resource.support.hash.DefaultHashBuildersProvider;
import ro.isdc.wro.model.resource.support.hash.HashBuilder;
import ro.isdc.wro.model.resource.support.hash.HashBuildersProvider;
import ro.isdc.wro.model.resource.support.naming.DefaultNamingStrategiesProvider;
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
  private ProcessorsProvider processorsProvider = new DefaultProcessorsProvider();
  private NamingStrategyProvider namingStrategiesProvider = new DefaultNamingStrategiesProvider();
  private HashBuildersProvider hashBuildersProvider = new DefaultHashBuildersProvider();
  
  /**
   * {@inheritDoc}
   */
  public java.util.Map<String,ResourcePreProcessor> providePreProcessors() {
    return processorsProvider.providePreProcessors();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return processorsProvider.providePostProcessors();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, HashBuilder> provideHashBuilders() {
    return hashBuildersProvider.provideHashBuilders();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, NamingStrategy> provideNamingStrategies() {
    return namingStrategiesProvider.provideNamingStrategies();
  }
}
