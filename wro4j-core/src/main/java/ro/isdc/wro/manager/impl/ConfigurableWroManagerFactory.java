/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.JSMinProcessor;
import ro.isdc.wro.processor.impl.JawrCssMinifierProcessor;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;


/**
 * TODO finish implementation & create unit tests.
 * <p>
 * Allow configuring uriLocators & processors to add using initParams of the filter.
 *
 * @author Alex Objelean
 * @created Created on Dec 31, 2009
 */
public class ConfigurableWroManagerFactory extends BaseWroManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableWroManagerFactory.class);
  /**
   * Name of init param used to specify uri locators.
   */
  private static final String PARAM_URI_LOCATORS = "uriLocators";
  private Map<String, ResourcePreProcessor> preProcessors = new HashMap<String, ResourcePreProcessor>();
  private Map<String, ResourcePostProcessor> postProcessors = new HashMap<String, ResourcePostProcessor>();
  private Map<String, UriLocator> locators = new HashMap<String, UriLocator>();

  public ConfigurableWroManagerFactory() {
    initProcessors();
    initLocators();
  }

  /**
   * Init locators with default values.
   */
  private void initLocators() {
    locators.put("servletContext", new ServletContextUriLocator());
    locators.put("classpath", new ClasspathUriLocator());
    locators.put("url", new UrlUriLocator());
    contributeLocators(locators);
  }

  /**
   * Init processors with default values.
   */
  private void initProcessors() {
    preProcessors.put("cssUrlRewriting", new CssUrlRewritingProcessor());
    postProcessors.put("cssVariables", new CssVariablesProcessor());
    postProcessors.put("cssMinJawr", new JawrCssMinifierProcessor());
    postProcessors.put("jsMin", new JSMinProcessor());
    contributePreProcessors(preProcessors);
    contributePostProcessors(postProcessors);

  }

  /**
   * Allow subclasses to contribute with it's own locators.
   * @param map containing locator mappings.
   */
  protected void contributeLocators(final Map<String, UriLocator> map) {
  }

  /**
   * Allow subclasses to contribute with it's own pre processors.
   * <p>
   * It is implementor responsibility to add a {@link ResourcePreProcessor} instance.
   *
   * @param map containing processor mappings.
   */
  protected void contributePreProcessors(final Map<String, ResourcePreProcessor> map) {
  }

  /**
   * Allow subclasses to contribute with it's own processors.
   * <p>
   * It is implementor responsibility to add a {@link ResourcePostProcessor} instance.
   *
   * @param map containing processor mappings.
   */
  protected void contributePostProcessors(final Map<String, ResourcePostProcessor> map) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    final List<UriLocator> locators = getLocators();
    for (final UriLocator uriLocator : locators) {
      factory.addUriLocator(uriLocator);
    }
    return factory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessorImpl groupProcessor = new GroupsProcessorImpl();
    groupProcessor.setResourcePreProcessors(preProcessors.values());
    groupProcessor.setResourcePostProcessors(postProcessors.values());
    return groupProcessor;
  }

  /**
   * @return a list of configured uriLocators.
   */
  private List<UriLocator> getLocators() {
    final List<UriLocator> list = new ArrayList<UriLocator>();
    final String uriLocators = Context.get().getFilterConfig().getInitParameter(PARAM_URI_LOCATORS);
    final String[] locatorsArray = uriLocators.split(",");
    for (final String locatorAsString : locatorsArray) {
      final UriLocator locator = locators.get(locatorAsString);
      if (locator == null) {
        throw new WroRuntimeException("Invalid locator name: " + locatorAsString);
      }
      try {
        LOG.debug("Found locator for name: " + locatorAsString + " : " + locator);
        list.add(locator);
      } catch (final Exception e) {
        throw new WroRuntimeException("Cannot instantiate locator of type: " + locator, e);
      }
    }
    return list;
  }
}
