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
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
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
public final class ConfigurableWroManagerFactory extends BaseWroManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableWroManagerFactory.class);
  /**
   * Name of init param used to specify uri locators.
   */
  private static final String PARAM_URI_LOCATORS = "uriLocators";
  private static Map<String, Class<?>> PROCESSORS_MAP = new HashMap<String, Class<?>>();
  static {
    PROCESSORS_MAP.put("cssUrlRewriting", CssUrlRewritingProcessor.class);
    PROCESSORS_MAP.put("cssVariables", CssUrlRewritingProcessor.class);
    PROCESSORS_MAP.put("cssMinJawr", CssUrlRewritingProcessor.class);
    PROCESSORS_MAP.put("jsMin", CssUrlRewritingProcessor.class);
  }
  private static Map<String, Class<? extends UriLocator>> URI_LOCATOR_MAP = new HashMap<String, Class<? extends UriLocator>>();
  static {
    URI_LOCATOR_MAP.put("servletContext", ServletContextUriLocator.class);
    URI_LOCATOR_MAP.put("classpath", ClasspathUriLocator.class);
    URI_LOCATOR_MAP.put("url", UrlUriLocator.class);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupProcessor = new GroupsProcessorImpl();
    return groupProcessor;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactory factory = new UriLocatorFactoryImpl();
    final List<UriLocator> locators = getLocators();
    for (final UriLocator uriLocator : locators) {
      factory.addUriLocator(uriLocator);
    }
    return factory;
  }


  /**
   * @return a list of configured uriLocators.
   */
  private List<UriLocator> getLocators() {
    final List<UriLocator> list = new ArrayList<UriLocator>();
    final String uriLocators = Context.get().getFilterConfig().getInitParameter(PARAM_URI_LOCATORS);
    final String[] locatorsArray = uriLocators.split(",");
    for (final String locatorAsString : locatorsArray) {
      final Class<? extends UriLocator> locator = URI_LOCATOR_MAP.get(locatorAsString);
      if (locator == null) {
        throw new WroRuntimeException("Invalid locator name: " + locatorAsString);
      }
      try {
        LOG.debug("Found locator for name: " + locatorAsString + " : " + locator);
        list.add(locator.newInstance());
      } catch (final Exception e) {
        throw new WroRuntimeException("Cannot instantiate locator of type: " + locator, e);
      }
    }
    return list;
  }
}
