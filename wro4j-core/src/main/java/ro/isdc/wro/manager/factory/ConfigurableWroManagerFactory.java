/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


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
  //Use LinkedHashMap to preserve the addition order
  private final Map<String, ResourcePreProcessor> preProcessors = new LinkedHashMap<String, ResourcePreProcessor>();
  private final Map<String, ResourcePostProcessor> postProcessors = new LinkedHashMap<String, ResourcePostProcessor>();
  private final Map<String, UriLocator> locators = new LinkedHashMap<String, UriLocator>();


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
    preProcessors.put("bomStripper", new BomStripperPreProcessor());
    preProcessors.put("cssImport", new CssImportPreProcessor());
    preProcessors.put("cssVariables", new CssVariablesProcessor());
    preProcessors.put("semicolonAppender", new SemicolonAppenderPreProcessor());
    preProcessors.put("cssDataUri", new CssDataUriPreProcessor());
    preProcessors.put("cssCompressor", new CssCompressorProcessor());
    preProcessors.put("cssMinJawr", new JawrCssMinifierProcessor());
    preProcessors.put("jsMin", new JSMinProcessor());
    preProcessors.put("variablizeColors", new VariablizeColorsCssProcessor());
    preProcessors.put("conformColors", new ConformColorsCssProcessor());

    postProcessors.put("cssVariables", new CssVariablesProcessor());
    postProcessors.put("cssCompressor", new CssCompressorProcessor());
    postProcessors.put("cssMinJawr", new JawrCssMinifierProcessor());
    postProcessors.put("jsMin", new JSMinProcessor());
    preProcessors.put("variablizeColors", new VariablizeColorsCssProcessor());
    preProcessors.put("conformColors", new ConformColorsCssProcessor());

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
  @Override
  protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
    for (final UriLocator locator : getLocators()) {
      factory.addUriLocator(locator);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected final void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
    groupsProcessor.setResourcePreProcessors(getPreProcessors());
    groupsProcessor.setResourcePostProcessors(getPostProcessors());
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
   * Extracts a list of items (processors) from init-param based on existing values inside the map.
   *
   * @param initParamName name of init-param that identifies required items.
   * @param map mapping between items and its implementations.
   * @return a list of items (processors).
   */
  private <T> List<T> getListOfItems(final String initParamName, final Map<String, T> map) {
    final List<T> list = new ArrayList<T>();
    final String paramValue = Context.get().getFilterConfig().getInitParameter(initParamName);
    LOG.debug("paramValue: " + paramValue);
    final List<String> tokens = getTokens(paramValue);
    if (tokens.isEmpty()) {
      final String message = "No '" + initParamName + "' initParam was set";
      LOG.warn(message);
      return list;
    }
    for (final String token : tokens) {
      final T item = map.get(token);
      if (item == null) {
        LOG.info("Available " + initParamName + " are: " + map.keySet());
        throw new WroRuntimeException("Invalid " + initParamName + " name: " + token);
      }
      LOG.debug("Found " + initParamName + " for name: " + token + " : " + item);
      list.add(item);
    }
    return list;
  }


  /**
   * Creates a list of tokens (processors name) based on provided string of comma separated strings.
   *
   * @param input string representation of tokens separated by ',' character.
   * @return a list of non empty strings.
   */
  private List<String> getTokens(final String input) {
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
