/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.spi.ServiceRegistry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.AbstractConfigurableMultipleStrategy;


/**
 * A {@link ProcessorsFactory} implementation which is easy to configure using a {@link Properties} object.
 * 
 * @author Alex Objelean
 * @created 30 Jul 2011
 * @since 1.4.0
 */
public class ConfigurableProcessorsFactory
    implements ProcessorsFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableProcessorsFactory.class);
  
  /**
   * Property name to specify pre processors.
   */
  public static final String PARAM_PRE_PROCESSORS = "preProcessors";
  /**
   * Property name to specify post processors.
   */
  public static final String PARAM_POST_PROCESSORS = "postProcessors";
  private Map<String, ResourceProcessor> preProcessorsMap;
  private Map<String, ResourceProcessor> postProcessorsMap;

  /**
   * Holds the configurations describing available processors.
   */
  private Properties properties;
  
  private final AbstractConfigurableMultipleStrategy<ResourceProcessor, ProcessorProvider> configurablePreProcessors = new AbstractConfigurableMultipleStrategy<ResourceProcessor, ProcessorProvider>() {
    @Override
    protected String getStrategyKey() {
      return PARAM_PRE_PROCESSORS;
    }
    
    @Override
    protected void overrideDefaultStrategyMap(final Map<String, ResourceProcessor> map) {
      copyAll(ConfigurableProcessorsFactory.this.getPreProcessorsMap(), map);
    }

    @Override
    protected Map<String, ResourceProcessor> getStrategies(final ProcessorProvider provider) {
      return provider.providePreProcessors();
    }
    
    @Override
    protected ResourceProcessor getStrategyForAlias(final String alias) {
      ResourceProcessor processor = super.getStrategyForAlias(alias);
      if (processor == null) {
        final String extension = FilenameUtils.getExtension(alias);
        boolean hasExtension = !StringUtils.isEmpty(extension);
        if (hasExtension) {
          final String processorName = FilenameUtils.getBaseName(alias);
          LOG.debug("processorName: {}", processorName);
          processor = super.getStrategyForAlias(processorName);
          if (processor != null) {
            LOG.debug("adding Extension: {}", extension);
            processor = ExtensionsAwareProcessorDecorator.decorate(processor).addExtension(extension);
          }
        }
      }
      return processor;
    }

    @Override
    protected Properties newProperties() {
      return ConfigurableProcessorsFactory.this.getProperties();
    };
  };

  private final AbstractConfigurableMultipleStrategy<ResourceProcessor, ProcessorProvider> configurablePostProcessors = new AbstractConfigurableMultipleStrategy<ResourceProcessor, ProcessorProvider>() {
    @Override
    protected String getStrategyKey() {
      return PARAM_POST_PROCESSORS;
    }
    
    @Override
    protected void overrideDefaultStrategyMap(final Map<String, ResourceProcessor> map) {
      copyAll(ConfigurableProcessorsFactory.this.getPostProcessorsMap(), map);
    }
    
    @Override
    protected Map<String, ResourceProcessor> getStrategies(final ProcessorProvider provider) {
      return provider.providePostProcessors();
    }
    
    @Override
    protected ResourceProcessor getStrategyForAlias(final String alias) {
      ResourceProcessor processor = super.getStrategyForAlias(alias);
      if (processor == null) {
        final String extension = FilenameUtils.getExtension(alias);
        boolean hasExtension = !StringUtils.isEmpty(extension);
        if (hasExtension) {
          final String processorName = FilenameUtils.getBaseName(alias);
          LOG.debug("processorName: {}", processorName);
          processor = super.getStrategyForAlias(processorName);
          if (processor != null) {
            LOG.debug("adding Extension: {}", extension);
            processor = ExtensionsAwareProcessorDecorator.decorate(new ProcessorDecorator(processor)).addExtension(
                extension);
          }
        }
      }
      return processor;
    }
    
    @Override
    protected Properties newProperties() {
      return ConfigurableProcessorsFactory.this.getProperties();
    };
  };

  /**
   * @return default implementation of {@link Properties} containing the list of pre & post processors.
   */
  protected Properties newProperties() {
    return new Properties();
  }
  
  /**
   * {@inheritDoc}
   */
  public final Collection<ResourceProcessor> getPreProcessors() {
    return configurablePreProcessors.getConfiguredStrategies();
  }
  
  /**
   * {@inheritDoc}
   */
  public final Collection<ResourceProcessor> getPostProcessors() {
    return configurablePostProcessors.getConfiguredStrategies();
  }
  
  /**
   * @param map
   *          containing preProcessors with corresponding alias (as key). The map must not be null and once set, the
   *          default map will be overridden.
   */
  public ConfigurableProcessorsFactory setPreProcessorsMap(final Map<String, ResourceProcessor> map) {
    Validate.notNull(map);
    this.preProcessorsMap = map;
    return this;
  }
  
  /**
   * @param map
   *          containing postProcessors with corresponding alias (as key). The map must not be null and once set, the
   *          default map will be overridden.
   */
  public ConfigurableProcessorsFactory setPostProcessorsMap(final Map<String, ResourceProcessor> map) {
    Validate.notNull(map);
    this.postProcessorsMap = map;
    return this;
  }
  
  public ConfigurableProcessorsFactory setProperties(final Properties properties) {
    Validate.notNull(properties);
    this.properties = properties;
    return this;
  }

  /**
   * By default the processor will be discovered using {@link ServiceRegistry} pattern (by inspecting META-INF/services
   * folder of each dependency).
   * 
   * @return a default map of preProcessors.
   */
  protected Map<String, ResourceProcessor> newPreProcessorsMap() {
    return new HashMap<String, ResourceProcessor>();
  }
  
  /**
   * By default the processor will be discovered using {@link ServiceRegistry} pattern (by inspecting META-INF/services
   * folder of each dependency).
   * 
   * @return a default map of postProcessors.
   */
  protected Map<String, ResourceProcessor> newPostProcessorsMap() {
    return new HashMap<String, ResourceProcessor>();
  }
  
  /**
   * To be used for internal usage. Ensure that returned object is not null.
   */
  private Properties getProperties() {
    if (this.properties == null) {
      this.properties = newProperties();
    }
    return this.properties;
  }
  
  /**
   * To be used for internal usage. Ensure that returned object is not null.
   */
  private Map<String, ResourceProcessor> getPreProcessorsMap() {
    if (this.preProcessorsMap == null) {
      synchronized (this) {
        if (this.preProcessorsMap == null) {
          this.preProcessorsMap = newPreProcessorsMap();
        }
      }
    }
    return this.preProcessorsMap;
  }
  
  /**
   * To be used for internal usage. Ensure that returned object is not null.
   */
  private Map<String, ResourceProcessor> getPostProcessorsMap() {
    if (this.postProcessorsMap == null) {
      synchronized (this) {
        if (this.postProcessorsMap == null) {
          this.postProcessorsMap = newPostProcessorsMap();
        }
      }
    }
    return this.postProcessorsMap;
  }

  /**
   * @VisibleForTesting
   * @return the list of all available pre processors;
   */
  Collection<ResourceProcessor> getAvailablePreProcessors() {
    return configurablePreProcessors.getAvailableStrategies();
  }
  
  /**
   * @VisibleForTesting
   * @return the list of all available pre processors;
   */
  Collection<ResourceProcessor> getAvailablePostProcessors() {
    return configurablePostProcessors.getAvailableStrategies();
  }
}
