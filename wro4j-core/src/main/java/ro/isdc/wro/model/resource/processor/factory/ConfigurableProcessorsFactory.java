/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.Validate;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * A {@link ProcessorsFactory} implementation which is easy to configure using a {@link Properties} object.
 *
 * @author Alex Objelean
 * @created 30 Jul 2011
 * @since 1.4.0
 */
public class ConfigurableProcessorsFactory implements ProcessorsFactory {
  private Properties properties = new Properties();
  private Map<String, ResourcePreProcessor> preProcessorsMap;
  private Map<String, ResourcePostProcessor> postProcessorsMap;

  public ConfigurableProcessorsFactory setPreProcessorsMap(final Map<String, ResourcePreProcessor> map) {
    preProcessorsMap = map;
    return this;
  }

  public ConfigurableProcessorsFactory setPostProcessorsMap(final Map<String, ResourcePostProcessor> map) {
    postProcessorsMap = map;
    return this;
  }

  public Map<String, ResourcePreProcessor> newPreProcessorsMap() {
    return new HashMap<String, ResourcePreProcessor>();
  }

  public Map<String, ResourcePostProcessor> newPostProcessorsMap() {
    return new HashMap<String, ResourcePostProcessor>();
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePreProcessor> getPreProcessors() {
    final List<ResourcePreProcessor> list = new ArrayList<ResourcePreProcessor>();
    final String preProcessorsAsString = properties.getProperty(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS);
    final String[] preProcessors = preProcessorsAsString.split(",");
    for (final String processor : preProcessors) {
      list.add(getPreProcessorByName(processor));
    }
    return list;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePostProcessor> getPostProcessors() {
    final List<ResourcePostProcessor> list = new ArrayList<ResourcePostProcessor>();
    final String postProcessorsAsString = properties.getProperty(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS);
    final String[] postProcessors = postProcessorsAsString.split(",");
    for (final String processor : postProcessors) {
      list.add(getPostProcessorByName(processor));
    }
    return list;
  }

  private ResourcePreProcessor getPreProcessorByName(final String processor) {
    Validate.notEmpty(processor, "Invalid PreProcessor name: " + processor);
    final ResourcePreProcessor preProcessor = preProcessorsMap.get(processor.trim());
    if (preProcessor == null) {
      throw new WroRuntimeException("Unknown preProcessor name: " + processor + ". Existing preProcessors are: "
        + preProcessorsMap.keySet());
    }
    return preProcessor;
  }

  private ResourcePostProcessor getPostProcessorByName(final String processor) {
    Validate.notEmpty(processor, "Invalid PreProcessor name: " + processor);
    final ResourcePostProcessor postProcessor = postProcessorsMap.get(processor.trim());
    if (postProcessor == null) {
      throw new WroRuntimeException("Unknown preProcessor name: " + postProcessor + ". Existing preProcessors are: "
        + postProcessorsMap.keySet());
    }
    return postProcessor;
  }
}
