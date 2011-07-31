/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterConfig;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
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
   * Name of init param used to specify uri locators.
   */
  public static final String PARAM_URI_LOCATORS = "uriLocators";

  private Map<String, UriLocator> createLocatorsMap() {
    final Map<String, UriLocator> map = new HashMap<String, UriLocator>();
    map.put("servletContext", new ServletContextUriLocator());
    map.put("classpath", new ClasspathUriLocator());
    map.put("url", new UrlUriLocator());
    return map;
  }

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


  /**
   * {@inheritDoc}
   */
  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    final SimpleUriLocatorFactory factory = new SimpleUriLocatorFactory();
    final Map<String, UriLocator> map = createLocatorsMap();
    contributeLocators(map);
    final String uriLocators = Context.get().getFilterConfig().getInitParameter(PARAM_URI_LOCATORS);
    final List<UriLocator> locators = ConfigurableProcessorsFactory.getListOfItems(uriLocators, map);
    for (final UriLocator locator : locators) {
      factory.addUriLocator(locator);
    }
    return factory;
  }


  /**
   * Reuse {@link ConfigurableProcessorsFactory} for processors lookup.
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final ConfigurableProcessorsFactory factory = new ConfigurableProcessorsFactory() {
      @Override
      public Map<String, ResourcePreProcessor> newPreProcessorsMap() {
        final Map<String, ResourcePreProcessor> map = ProcessorsUtils.createPreProcessorsMap();
        contributePreProcessors(map);
        return map;
      }


      @Override
      public Map<String, ResourcePostProcessor> newPostProcessorsMap() {
        final Map<String, ResourcePostProcessor> map = ProcessorsUtils.createPostProcessorsMap();
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
