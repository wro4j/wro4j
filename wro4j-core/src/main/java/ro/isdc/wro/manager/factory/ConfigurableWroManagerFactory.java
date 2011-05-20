/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.DuplicatesAwareCssDataUriPreProcessor;
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
  private final Map<String, ResourcePreProcessor> postProcessors = new LinkedHashMap<String, ResourcePreProcessor>();


  /**
   * Initialize processors & locators with a default list.
   */
  public ConfigurableWroManagerFactory() {
    initProcessors();
  }

  /**
   * Init processors with default values.
   */
  private void initProcessors() {
    preProcessors.putAll(createCommonProcessors());
    preProcessors.put("cssUrlRewriting", new CssUrlRewritingProcessor());
    preProcessors.put("cssImport", new CssImportPreProcessor());
    preProcessors.put("semicolonAppender", new SemicolonAppenderPreProcessor());
    preProcessors.put("cssDataUri", new CssDataUriPreProcessor());
    preProcessors.put("duplicateAwareCssDataUri", new DuplicatesAwareCssDataUriPreProcessor());
    postProcessors.putAll(createCommonProcessors());

    contributePreProcessors(preProcessors);
    contributePostProcessors(postProcessors);
  }


  /**
   * @return a map of processors to be used as both: pre & post processor.
   */
  private Map<String, ResourcePreProcessor> createCommonProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put("bomStripper", new BomStripperPreProcessor());
    map.put("cssVariables", new CssVariablesProcessor());
    map.put("cssCompressor", new CssCompressorProcessor());
    map.put("cssMinJawr", new JawrCssMinifierProcessor());
    map.put("jsMin", new JSMinProcessor());
    map.put("variablizeColors", new VariablizeColorsCssProcessor());
    map.put("conformColors", new ConformColorsCssProcessor());
    map.put("cssVariables", new CssVariablesProcessor());
    return map;

  }

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
  protected void contributePostProcessors(final Map<String, ResourcePreProcessor> map) {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.setResourcePreProcessors(getPreProcessors());
    factory.setResourcePostProcessors(getPostProcessors());
    return factory;
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
  List<ResourcePreProcessor> getPostProcessors() {
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
