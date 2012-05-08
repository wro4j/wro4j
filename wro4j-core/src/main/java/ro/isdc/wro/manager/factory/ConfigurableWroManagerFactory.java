/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterConfig;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.factory.FilterConfigWroConfigurationFactory;
import ro.isdc.wro.config.factory.ServletContextPropertyWroConfigurationFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * Allow configuring uriLocators & processors to add using initParams of the filter.
 *
 * @author Alex Objelean
 * @created Created on Dec 31, 2009
 */
public class ConfigurableWroManagerFactory extends BaseWroManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableWroManagerFactory.class);
  private Properties configProperties;
  /**
   * Allow subclasses to contribute with it's own pre processors.
   * <p>
   * It is implementor responsibility to add a {@link ResourceProcessor} instance.
   *
   * @param map containing processor mappings.
   */
  protected void contributePreProcessors(final Map<String, ResourceProcessor> map) {}


  /**
   * Allow subclasses to contribute with it's own processors.
   * <p>
   * It is implementor responsibility to add a {@link ResourcePostProcessor} instance.
   *
   * @param map containing processor mappings.
   */
  protected void contributePostProcessors(final Map<String, ResourceProcessor> map) {}


  /**
   * Reuse {@link ConfigurableProcessorsFactory} for processors lookup.
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final ConfigurableProcessorsFactory factory = new ConfigurableProcessorsFactory() {
      @Override
      public Map<String, ResourceProcessor> newPreProcessorsMap() {
        final Map<String, ResourceProcessor> map = ProcessorsUtils.createProcessorsMap();
        contributePreProcessors(map);
        return map;
      }


      @Override
      public Map<String, ResourceProcessor> newPostProcessorsMap() {
        final Map<String, ResourceProcessor> map = ProcessorsUtils.createProcessorsMap();
        contributePostProcessors(map);
        return map;
      }


      @Override
      protected Properties newProperties() {
        final Properties props = new Properties();
        updatePropertiesWithProcessors(props, ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS);
        updatePropertiesWithProcessors(props, ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS);
        return props;
      }


      /**
       * Add to properties a new key with value extracted either from filterConfig or from configurable properties file.
       */
      private void updatePropertiesWithProcessors(final Properties props, final String paramName) {
        final FilterConfig filterConfig = Context.get().getFilterConfig();
        // first, retrieve value from init-param for backward compatibility
        final String processorsAsString = filterConfig.getInitParameter(paramName);
        if (processorsAsString != null) {
          props.setProperty(paramName, processorsAsString);
        } else {
          // retrieve value from configProperties file
          final String value = getConfigProperties().getProperty(paramName);
          if (value != null) {
            props.setProperty(paramName, value);
          }
        }
      }
    };
    return factory;
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
      LOG.debug("No configuration property file found.");
    }
    return props;
  }


  /**
   * Use this method rather than accessing the field directly, because it will create a default one if none is provided.
   */
  private Properties getConfigProperties() {
    if (configProperties == null) {
      configProperties = newConfigProperties();
    }
    return configProperties;
  }


  /**
   * Setter is useful for unit tests.
   */
  public ConfigurableWroManagerFactory setConfigProperties(final Properties configProperties) {
    Validate.notNull(configProperties);
    this.configProperties = configProperties;
    return this;
  }

}
