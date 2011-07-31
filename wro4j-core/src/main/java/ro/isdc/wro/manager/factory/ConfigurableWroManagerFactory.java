/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterConfig;

import ro.isdc.wro.config.Context;
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
        final FilterConfig filterConfig = Context.get().getFilterConfig();

        final String preProcessorsAsString = filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS);
        if (preProcessorsAsString != null) {
          props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, preProcessorsAsString);
        }

        final String postProcessorsAsString = filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS);
        if (postProcessorsAsString != null) {
          props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, postProcessorsAsString);
        }
        return props;
      }
    };
    return factory;
  }
}
