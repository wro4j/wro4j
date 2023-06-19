/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterConfig;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.ConfigurableCacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.factory.FilterConfigWroConfigurationFactory;
import ro.isdc.wro.config.factory.ServletContextPropertyWroConfigurationFactory;
import ro.isdc.wro.model.factory.ConfigurableModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.ConfigurableLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.ConfigurableHashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.ConfigurableNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;


/**
 * Allow configuring uriLocators and processors to add using initParams of the filter.
 *
 * @author Alex Objelean
 */
public class ConfigurableWroManagerFactory extends BaseWroManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableWroManagerFactory.class);
  private Properties configProperties;
  private Properties additionalConfigProperties;

  /**
   * Allow subclasses to contribute with it's own locators.
   *
   * @param map containing locator mappings.
   */
  protected void contributeLocators(final Map<String, UriLocator> map) {}


  /**
   * Allow subclasses to contribute with it's own pre processors.
   * <p>
   * It is implementor responsibility to add a {@link ResourcePreProcessor} instance.
   *
   * @param map containing processor mappings.
   */
  protected void contributePreProcessors(final Map<String, ResourcePreProcessor> map) {}


  /**
   * Allow subclasses to contribute with it's own processors.
   * <p>
   * It is implementor responsibility to add a {@link ResourcePostProcessor} instance.
   *
   * @param map containing processor mappings.
   */
  protected void contributePostProcessors(final Map<String, ResourcePostProcessor> map) {}


  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    return new ConfigurableLocatorFactory() {
      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithConfiguration(props, ConfigurableLocatorFactory.PARAM_URI_LOCATORS);
        return props;
      }
      @Override
      protected Map<String, UriLocator> getStrategies(final LocatorProvider provider) {
        final Map<String, UriLocator> map = super.getStrategies(provider);
        contributeLocators(map);
        return map;
      }
    };
  }


  /**
   * Reuse {@link ConfigurableProcessorsFactory} for processors lookup.
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    return new ConfigurableProcessorsFactory() {
      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithConfiguration(props, ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS);
        updatePropertiesWithConfiguration(props, ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS);
        return props;
      }
      @Override
      protected Map<String, ResourcePostProcessor> getPostProcessorStrategies(final ProcessorProvider provider) {
        final Map<String, ResourcePostProcessor> map = super.getPostProcessorStrategies(provider);
        contributePostProcessors(map);
        return map;
      }
      @Override
      protected Map<String, ResourcePreProcessor> getPreProcessorStrategies(final ProcessorProvider provider) {
        final Map<String, ResourcePreProcessor> map = super.getPreProcessorStrategies(provider);
        contributePreProcessors(map);
        return map;
      }
    };
  }

  @Override
  protected NamingStrategy newNamingStrategy() {
    return new ConfigurableNamingStrategy() {
      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithConfiguration(props, ConfigurableNamingStrategy.KEY);
        return props;
      }
    };
  }

  @Override
  protected HashStrategy newHashStrategy() {
    return new ConfigurableHashStrategy() {
      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithConfiguration(props, ConfigurableHashStrategy.KEY);
        return props;
      }
    };
  }

  @Override
  protected CacheStrategy<CacheKey, CacheValue> newCacheStrategy() {
    return new ConfigurableCacheStrategy() {
      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithConfiguration(props, ConfigurableCacheStrategy.KEY);
        return props;
      }
    };
  }

  @Override
  protected WroModelFactory newModelFactory() {
    return new ConfigurableModelFactory() {
      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithConfiguration(props, ConfigurableModelFactory.KEY);
        return props;
      }
    };
  }

  /**
   * Add to properties a new key with value extracted either from filterConfig or from configurable properties file.
   * This method helps to ensure backward compatibility of the filterConfig vs configProperties configuration.
   *
   * @param props
   *          the {@link Properties} which will be populated with the value extracted from filterConfig or
   *          configProperties for the provided key.
   * @param key
   *          to read from filterConfig or configProperties and put into props.
   */
  private void updatePropertiesWithConfiguration(final Properties props, final String key) {
    final FilterConfig filterConfig = Context.get().getFilterConfig();
    // first, retrieve value from init-param for backward compatibility
    final String valuesAsString = filterConfig.getInitParameter(key);
    if (valuesAsString != null) {
      props.setProperty(key, valuesAsString);
    } else {
      // retrieve value from configProperties file
      final String value = getConfigProperties().getProperty(key);
      if (value != null) {
        props.setProperty(key, value);
      }
    }
  }

  /**
   * Use this method rather than accessing the field directly, because it will create a default one if none is provided.
   */
  private Properties getConfigProperties() {
    if (configProperties == null) {
      configProperties = newConfigProperties();
      if (additionalConfigProperties != null) {
        configProperties.putAll(additionalConfigProperties);
      }
    }
    return configProperties;
  }

  /**
   * Override this method to provide a different config properties file location. It is very likely that you would like
   * it to be the same as the one used by the {@link FilterConfigWroConfigurationFactory}. The default properties file
   * location is /WEB-INF/wro.properties.
   *
   * @return a not null properties object used as a secondary configuration option for processors if these are not
   *         configured in init-param.
   */
  protected Properties newConfigProperties() {
    // default location is /WEB-INF/wro.properties
    final Properties props = new Properties();
    try {
      return new ServletContextPropertyWroConfigurationFactory(Context.get().getServletContext()).createProperties();
    } catch (final Exception e) {
      LOG.warn("No configuration property file found. Using default values.", e);
    }
    return props;
  }

  ConfigurableWroManagerFactory addConfigProperties(final Properties configProperties) {
    Validate.notNull(configProperties);
    this.additionalConfigProperties = configProperties;
    return this;
  }

  /**
   * Setter is useful for unit tests.
   */
  ConfigurableWroManagerFactory setConfigProperties(final Properties configProperties) {
    Validate.notNull(configProperties);
    this.configProperties = configProperties;
    return this;
  }
}
