/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.AbstractGroupsProcessor;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactoryImpl;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.JawrCssMinifierProcessor;


/**
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
  public static final String PARAM_URI_LOCATORS = "uriLocators";
  /**
   * Name of init param used to specify pre processors.
   */
  public static final String PARAM_PRE_PROCESSORS = "preProcessors";
  /**
   * Name of init param used to specify post processors.
   */
  public static final String PARAM_POST_PROCESSORS = "postProcessors";

  /**
   * Delimit tokens containing a list of locators, preProcessors & postProcessors.
   */
  private static final String TOKEN_DELIMITER = ",";
  private final Map<String, ResourcePreProcessor> preProcessors = new HashMap<String, ResourcePreProcessor>();
  private final Map<String, ResourcePostProcessor> postProcessors = new HashMap<String, ResourcePostProcessor>();
  private final Map<String, UriLocator> locators = new HashMap<String, UriLocator>();

  /**
   * Initialize processors & locators with a default list.
   */
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

    final CssImportPreProcessor cssImportProcessor = new CssImportPreProcessor();
    preProcessors.put("bomStripper", new BomStripperPreProcessor());
    preProcessors.put("cssImport", cssImportProcessor);
    postProcessors.put("cssVariables", new CssVariablesProcessor());
    postProcessors.put("cssMinJawr", new JawrCssMinifierProcessor());
    postProcessors.put("jsMin", new JSMinProcessor());
    contributePreProcessors(preProcessors);
    contributePostProcessors(postProcessors);
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
  private UriLocatorFactory newUriLocatorFactory() {
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
    final AbstractGroupsProcessor groupsProcessor = new GroupsProcessorImpl();
    groupsProcessor.setUriLocatorFactory(newUriLocatorFactory());
    groupsProcessor.setResourcePreProcessors(preProcessors.values());
    groupsProcessor.setResourcePostProcessors(postProcessors.values());
    return groupsProcessor;
  }


  /**
   * This method has friendly modifier in order to be able to test it.
   *
   * @return a list of configured uriLocators.
   */
  List<UriLocator> getLocators() {
    return getListOfItems(PARAM_URI_LOCATORS, locators);
  }

  /**
   * @return a list of configured preProcessors.
   */
  List<ResourcePreProcessor> getPreProcessors() {
    return getListOfItems(PARAM_PRE_PROCESSORS, preProcessors);
  }

  /**
   * @return a list of configured preProcessors.
   */
  List<ResourcePostProcessor> getPostProcessors() {
    return getListOfItems(PARAM_POST_PROCESSORS, postProcessors);
  }


  /**
   * @param initParamName name of init-param which identify what kind of items a required.
   * @param map mapping between items and its implementations.
   * @return a list of instances.
   */
  private <T>List<T> getListOfItems(final String initParamName, final Map<String, T> map) {
    final List<T> list = new ArrayList<T>();
    final String paramValue = Context.get().getFilterConfig().getInitParameter(initParamName);
    LOG.debug("paramValue: " + paramValue);
    final List<String> itemsList = getItemsList(paramValue);
    if (itemsList.isEmpty()) {
      final String message = "No '" + initParamName + "' initParam was set";
      throw new WroRuntimeException(message);
    }
    for (final String itemAsString : itemsList) {
      final T item = map.get(itemAsString);
      if (item == null) {
        LOG.info("Available " + initParamName + " are: " + map.keySet());
        throw new WroRuntimeException("Invalid " + initParamName + " name: " + itemAsString);
      }
      LOG.debug("Found " + initParamName + " for name: " + itemAsString + " : " + item);
      list.add(item);
    }
    return list;
  }


  /**
   * @param input string representation of tokens separated by ',' character.
   * @return a list of non empty strings.
   */
  private List<String> getItemsList(final String input) {
    final List<String> locatorsList = new ArrayList<String>();
    if (input != null) {
      // use StringTokenizer instead of split because it skips empty (but not trimmed) strings
      final StringTokenizer st = new StringTokenizer(input, TOKEN_DELIMITER);
      while (st.hasMoreTokens()) {
        final String token = st.nextToken().trim();
        if (!StringUtils.isEmpty(token)) {
          locatorsList.add(token);
        }
      }
    }
    return locatorsList;
  }
}
