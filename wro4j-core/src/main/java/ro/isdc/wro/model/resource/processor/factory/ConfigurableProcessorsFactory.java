/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.spi.ServiceRegistry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.ProcessorsContributor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;


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
   * Delimit tokens containing a list of locators, preProcessors & postProcessors.
   */
  private static final String TOKEN_DELIMITER = ",";

  /**
   * Property name to specify pre processors.
   */
  public static final String PARAM_PRE_PROCESSORS = "preProcessors";
  /**
   * Property name to specify post processors.
   */
  public static final String PARAM_POST_PROCESSORS = "postProcessors";
  /**
   * Properties object
   */
  private Properties properties;
  private Map<String, ResourcePreProcessor> preProcessorsMap;
  private Map<String, ResourcePostProcessor> postProcessorsMap;

  /**
   * @return default implementation of {@link Properties} containing the list of pre & post processors.
   */
  protected Properties newProperties() {
    return new Properties();
  }

  /**
   * Creates a list of tokens (processors name) based on provided string of comma separated strings.
   * 
   * @param input
   *          string representation of tokens separated by ',' character.
   * @return a list of non empty strings.
   */
  private static List<String> getTokens(final String input) {
    final List<String> list = new ArrayList<String>();
    if (!StringUtils.isEmpty(input)) {
      final String[] tokens = input.split(TOKEN_DELIMITER);
      for (final String token : tokens) {
        list.add(token.trim());
      }
    }
    return list;
  }

  /**
   * Creates a comma separated list of items.
   */
  public static String createItemsAsString(final String... items) {
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i < items.length; i++) {
      sb.append(items[i]);
      if (i < items.length - 1) {
        sb.append(TOKEN_DELIMITER);
      }
    }
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   */
  public final Collection<ResourcePreProcessor> getPreProcessors() {
    final String processorsAsString = getProperties().getProperty(PARAM_PRE_PROCESSORS);
    return getListOfItems(processorsAsString, getPreProcessorsMap());
  }

  /**
   * {@inheritDoc}
   */
  public final Collection<ResourcePostProcessor> getPostProcessors() {
    final String processorsAsString = getProperties().getProperty(PARAM_POST_PROCESSORS);
    return getListOfItems(processorsAsString, getPostProcessorsMap());
  }

  /**
   * Extracts a list of items (processors) from the properties based on existing values inside the map.
   * 
   * @param itemsAsString
   *          a comma separated list of items.
   * @param map
   *          mapping between items and its implementations.
   * @return a list of items (processors).
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> getListOfItems(final String itemsAsString, final Map<String, T> map) {
    Validate.notNull(map);
    LOG.debug("itemsAsString: " + itemsAsString);
    final List<T> list = new ArrayList<T>();
    final List<String> tokenNames = getTokens(itemsAsString);
    for (final String tokenName : tokenNames) {
      LOG.debug("\ttokenName: {}", tokenName);
      Validate.notEmpty(tokenName);
      T processor = map.get(tokenName.trim());
      if (processor == null) {
        
        // extension check
        LOG.debug("[FAIL] no processor found named: {}. Proceeding with extension check. ", tokenName);
        final String extension = FilenameUtils.getExtension(tokenName);
        boolean hasExtension = !StringUtils.isEmpty(extension);
        if (hasExtension) {
          final String processorName = FilenameUtils.getBaseName(tokenName);
          LOG.debug("processorName: {}", processorName);
          processor = map.get(processorName);
          if (processor != null && processor instanceof ResourcePreProcessor) {
            LOG.debug("adding Extension: {}", extension);
            processor = (T) ExtensionsAwareProcessorDecorator.decorate((ResourcePreProcessor) processor).addExtension(
                extension);
          }
        }
        if (processor == null) {
          throw new WroRuntimeException("Unknown processor name: " + tokenName + ". Available processors are: "
              + map.keySet()).logError();
        }
      }
      list.add(processor);
    }
    return list;
  }

  public ConfigurableProcessorsFactory setPreProcessorsMap(final Map<String, ResourcePreProcessor> map) {
    Validate.notNull(map);
    preProcessorsMap = map;
    return this;
  }

  public ConfigurableProcessorsFactory setPostProcessorsMap(final Map<String, ResourcePostProcessor> map) {
    Validate.notNull(map);
    postProcessorsMap = map;
    return this;
  }

  public ConfigurableProcessorsFactory setProperties(final Properties properties) {
    Validate.notNull(properties);
    this.properties = properties;
    return this;
  }
  
  /**
   * @return the list of all {@link ProcessorsContributor} found in classpath.
   */
  private List<ProcessorsContributor> discoverProcessorsContributors() {
    final Iterator<ProcessorsContributor> iterator = ServiceRegistry.lookupProviders(ProcessorsContributor.class);
    final List<ProcessorsContributor> contributors = new ArrayList<ProcessorsContributor>();
    for (; iterator.hasNext();) {
      contributors.add(iterator.next());
    }
    return contributors;
  }

  /**
   * @return a default map of preProcessors.
   */
  public Map<String, ResourcePreProcessor> newPreProcessorsMap() {
    final Map<String, ResourcePreProcessor> resultMap = new HashMap<String, ResourcePreProcessor>();
    final List<ProcessorsContributor> contributorList = discoverProcessorsContributors();
    for (ProcessorsContributor contributor : contributorList) {
      final Map<String, ResourcePreProcessor> contributionMap = contributor.contributePreProcessors();
      for (Map.Entry<String, ResourcePreProcessor> entry : contributionMap.entrySet()) {
        final String alias = entry.getKey();
        if (resultMap.containsKey(alias)) {
          LOG.warn("Duplicate ALIAS found: {}. Overriding old processor with new one.", alias);
        }
        resultMap.put(alias, entry.getValue());
      }
    }
    return resultMap;
  }
  
  /**
   * TODO: reuse duplicated code.
   * 
   * @return a default map of postProcessors.
   */
  public Map<String, ResourcePostProcessor> newPostProcessorsMap() {
    final Map<String, ResourcePostProcessor> resultMap = new HashMap<String, ResourcePostProcessor>();
    final List<ProcessorsContributor> contributorList = discoverProcessorsContributors();
    for (ProcessorsContributor contributor : contributorList) {
      final Map<String, ResourcePostProcessor> contributionMap = contributor.contributePostProcessors();
      for (Map.Entry<String, ResourcePostProcessor> entry : contributionMap.entrySet()) {
        final String alias = entry.getKey();
        if (resultMap.containsKey(alias)) {
          LOG.warn("Duplicate ALIAS found: {}. Overriding old processor with new one.", alias);
        }
        resultMap.put(alias, entry.getValue());
      }
    }
    return resultMap;
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
  private Map<String, ResourcePreProcessor> getPreProcessorsMap() {
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
  private Map<String, ResourcePostProcessor> getPostProcessorsMap() {
    if (this.postProcessorsMap == null) {
      synchronized (this) {
        if (this.postProcessorsMap == null) {
          this.postProcessorsMap = newPostProcessorsMap();
        }
      }
    }
    return this.postProcessorsMap;
  }
}
